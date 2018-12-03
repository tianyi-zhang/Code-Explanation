
# coding: utf-8

# In[1]:

from pathlib import Path
import logging
import wget
import pickle
from typing import List, Callable, Union, Any
from more_itertools import chunked
from itertools import chain
import nmslib
from pathos.multiprocessing import Pool, cpu_count
from math import ceil


# In[2]:

def apply_parallel(func: Callable, data: List[Any], cpu_cores: int=None) -> List[Any]:
    if not cpu_cores:
        cpu_cores = cpu_count()
    try: 
        chunk_size = ceil(len(data) / cpu_cores)
        print(chunk_size)
        # pool = Pool(cpu_cores)
        # print("pool")
        chunks = chunked(data, chunk_size)
        # print("was chunked")
        transformed_data = map(func, data)#chunks, chunksize=1)
        print(type(transformed_data))
        print("data was transformed")
    finally: 
        # pool.close()
        # pool.join()
        return list(transformed_data)


# In[3]:

def flattenlist(listoflists: List[List[Any]]):
    return list(chain.from_iterable(listoflists))


def read_training_files(data_path: str):
    # Read data from directory
    PATH = Path(data_path)
    with open(PATH/'train.function', 'r') as f:
        t_enc = f.readlines()
    with open(PATH/'valid.function', 'r') as f:
        v_enc = f.readlines()
    tv_enc = t_enc + v_enc # Combine training and validation sets and let keras randomly split it

    with open(PATH/'test.function', 'r', encoding="utf8") as f:
        h_enc = f.readlines()

    with open(PATH/'train.docstring', 'r') as f:
        t_dec = f.readlines()
    with open(PATH/'valid.docstring', 'r') as f:
        v_dec = f.readlines()
    tv_dec = t_dec + v_dec # Combine train and valid sets and let keras randomly split

    with open(PATH/'test.docstring', 'r', encoding="utf8") as f:
        h_dec = f.readlines()

    logging.warning(f'Num rows for encoder for training + validation input: {len(tv_enc):,}')
    logging.warning(f'Num rows for encoder holdout input: {len(h_enc):,}')
    logging.warning(f'Num rows for decoder training + validation input: {len(tv_dec):,}')
    logging.warning(f'Num rows for decoder holdout input: {len(h_dec):,}')

    return tv_enc, h_enc, tv_dec, h_dec


processed_data_filenames = [
'https://storage.googleapis.com/kubeflow-examples/code_search/data/test.docstring',
'https://storage.googleapis.com/kubeflow-examples/code_search/data/test.function',
'https://storage.googleapis.com/kubeflow-examples/code_search/data/test.lineage',
'https://storage.googleapis.com/kubeflow-examples/code_search/data/test_original_function.json.gz',
'https://storage.googleapis.com/kubeflow-examples/code_search/data/train.docstring',
'https://storage.googleapis.com/kubeflow-examples/code_search/data/train.function',
'https://storage.googleapis.com/kubeflow-examples/code_search/data/train.lineage',
'https://storage.googleapis.com/kubeflow-examples/code_search/data/train_original_function.json.gz',
'https://storage.googleapis.com/kubeflow-examples/code_search/data/valid.docstring',
'https://storage.googleapis.com/kubeflow-examples/code_search/data/valid.function',
'https://storage.googleapis.com/kubeflow-examples/code_search/data/valid.lineage',
'https://storage.googleapis.com/kubeflow-examples/code_search/data/valid_original_function.json.gz',
'https://storage.googleapis.com/kubeflow-examples/code_search/data/without_docstrings.function',
'https://storage.googleapis.com/kubeflow-examples/code_search/data/without_docstrings.lineage',
'https://storage.googleapis.com/kubeflow-examples/code_search/data/without_docstrings_original_function.json.gz']


def get_step2_prerequisite_files(output_directory):
    outpath = Path(output_directory)
    assert not list(outpath.glob('*')), f'There are files in {str(outpath.absolute())}, please clear files or specify an empty folder.'
    # outpath.mkdir(exist_ok=True)
    print(f'Saving files to {str(outpath.absolute())}')
    for url in processed_data_filenames:
        print(f'downloading {url}')
        wget.download(url, out=str(outpath.absolute()))

