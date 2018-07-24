
# coding: utf-8

# # Part 1 - Acquire and Parse Data

# Preprocess Data

# In[1]:

# %load_ext autoreload
# %get_ipython().magic('autoreload 2')

import ast
import glob
import re
from pathlib import Path

import astor
import pandas as pd
import spacy
from tqdm import tqdm
from nltk.tokenize import RegexpTokenizer
from sklearn.model_selection import train_test_split

from general_utils import apply_parallel, flattenlist

EN = spacy.load('en_core_web_md')


# In[2]:

# get_ipython().system('python -V')


# Download and Read Raw Python Files into Pandas DataFrame

# In[3]:

# get_ipython().run_cell_magic('time', '', "\n
df = pd.concat([pd.read_csv(f"https://storage.googleapis.com/kubeflow-examples/code_search/raw_data/00000000000{i}.csv") for i in range(1)], axis=1)
df['nwo'] = df['repo_path'].apply(lambda r: r.split()[0])
df['path'] = df['repo_path'].apply(lambda r: r.split()[1])
df.drop('repo_path', axis=1, inplace=True)
df = df[['nwo', 'path', 'content']]
df = df.truncate(after=99)


# Functions to Parse Data and Tokenize

# In[4]:

def tokenize_docstring(text):
    # Apply tokenization using spacy to docstrings
    tokens = EN.tokenizer(text)
    return [token.text.lower() for token in tokens if not token.is_space]
    
def tokenize_code(text):
    # A very basic procedure for tokennizing code strings
    return RegexpTokenizer(r'\w+').tokenize(text)

def get_function_docstring_pairs(blob):
    # Extract (function/method, docstring) pairs from a give code blob
    pairs = []
    try: 
        module = ast.parse(blob)
        classes = [node for node in module.body if isinstance(node, ast.ClassDef)]
        functions = [node for node in module.body if isinstance(node, ast.FunctionDef)]
        for c in classes: 
            functions.extend([node for node in c.body if isinstance(node, ast.FunctionDef)])
        for f in functions: 
            source = astor.to_source(f)
            docstring = ast.get_docstring(f) if ast.get_docstring(f) else ''
            function = source.replace(ast.get_docstring(f, clean=False), '') if docstring else source
            pairs.append((f.name,
                          f.lineno, 
                          source,
                          ' '.join(tokenize_code(function)),
                          ' '.join(tokenize_docstring(docstring.split('\n\n')[0]))
                         ))
    except (AssertionError, MemoryError, SyntaxError, UnicodeEncodeError):
        pass
    # print("pairs length")
    # print(len(pairs))
    return pairs

def get_function_docstring_pairs_list(blob_list):
    # Apply the above function on a list of code blobs
    return [get_function_docstring_pairs(b) for b in blob_list]


# In[5]:
content_list = df.content.tolist()

# pairs = flattenlist(apply_parallel(get_function_docstring_pairs, content_list, cpu_cores=4))
pairs = apply_parallel(get_function_docstring_pairs, content_list, cpu_cores=4)


# In[6]:

assert len(pairs) == df.shape[0], 'Row count mismatch. df has {df.shape[0]:,} rows; pairs has {len(pairs):,} rows.'
df['pairs'] = pairs


# flatten pairs
df = df.set_index(['nwo', 'path'])['pairs'].apply(pd.Series).stack()
df = df.reset_index()
df.columns = ['nwo', 'path', '_', 'pair']


# extract metadata and format dataframe
df['function_name'] = df['pair'].apply(lambda p: p[0])
df['lineno'] = df['pair'].apply(lambda p: p[1])
df['original_function'] = df['pair'].apply(lambda p: p[2])
df['function_tokens'] = df['pair'].apply(lambda p: p[3])
df['docstring_tokens'] = df['pair'].apply(lambda p: p[4])
df = df[['nwo', 'path', 'function_name', 'lineno', 'original_function', 'function_tokens', 'docstring_tokens']]
# df['url'] = df[['nwo', 'path', 'lineno']].apply(lambda x: 'https://github.com/{}/blob/master/{}#L{}'.format(x[0], x[1], x[2]), axis=1)


# Remove Duplicates
df.drop_duplicates(['original_function', 'function_tokens'])
print(df.shape)


# Separate functions without docstrings
def listlen(x):
    if not isinstance(x, list):
        return 0
    return len(x)
with_docstrings = df[df.docstring_tokens.str.split().apply(listlen) >= 3]
without_docstrings = df[df.docstring_tokens.str.split().apply(listlen) < 3]


# Partition code by repository to minimize leakage between train, valid, and test sets
grouped = with_docstrings.groupby('nwo')
train, test = train_test_split(list(grouped), train_size=0.87, random_state=8081)
train, valid = train_test_split(train, train_size=0.82, random_state=8081)

train = pd.concat([d for _, d in train]).reset_index(drop=True)
valid = pd.concat([d for _, d in valid]).reset_index(drop=True)
test = pd.concat([d for _, d in test]).reset_index(drop=True)

print(f'train set num rows {train.shape[0]:,}')
print(f'valid set num rows {valid.shape[0]:,}')
print(f'test set num rows {test.shape[0]:,}')
print(f'without docstring rows {without_docstrings.shape[0]:,}')


#Output each set to train/valid/test.function/docstrings/lineage files
def write_to(df, filename, path='./data/processed_data/'):
    "Helper function to write processed files to disk."
    out = Path(path)
    # out.mkdir(exist_ok=True)
    df.function_tokens.to_csv(out/'{}.function'.format(filename), index=False)
    df.original_function.to_json(out/'{}_original_function.json.gz'.format(filename), orient='values', compression='gzip')
    if filename != 'without_docstrings':
        df.docstring_tokens.to_csv(out/'{}.docstring'.format(filename), index=False)

# write to output files
write_to(train, 'train')
write_to(valid, 'valid')
write_to(test, 'test')
write_to(without_docstrings, 'without_docstrings')

#train.to_csv('data/processed_data/train.csv')
#valid.to_csv('data/processed_data/valid.csv')
#test.to_csv('data/processed_data/test.csv')