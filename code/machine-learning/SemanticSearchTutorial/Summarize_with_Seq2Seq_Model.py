use_cache = False

from pathlib import Path
from general_utils import get_step2_prerequisite_files, read_training_files
from keras.utils import get_file
import logging
OUTPUT_PATH = Path('./data/seq2seq/')
# OUTPUT_PATH.mkdir(exist_ok=True)


# Read text from file
# if use_cache:
#     get_step2_prerequisite_files(output_directory = './data/processed_data')

# you want to supply the directory where the files are from step 1.
train_code, holdout_code, train_comment, holdout_comment = read_training_files('./data/processed_data/')
assert len(train_code) == len(train_comment)
assert len(holdout_code) == len(holdout_comment)


# Tokenize Text
from ktext.preprocess import processor
if not use_cache:
    code_proc = processor(hueristic_pct_padding=.7, keep_n=20)
    t_code = code_proc.fit_transform(train_code)
    comment_proc = processor(append_indicators=True, hueristic_pct_padding=.7, keep_n=14, padding='post')
    t_comment = comment_proc.fit_transform(train_comment)
elif use_cache:
    logging.warning('Not fitted to transform function because use_cache=True')

# Save Tokenized Text
import dill as dpickle
import numpy as np
if not use_cache:
    # Save the preprocessor
    with open(OUTPUT_PATH/'py_code_proc_v2.dpkl', 'wb') as f:
        dpickle.dump(code_proc, f)
    with open(OUTPUT_PATH/'py_comment_proc_v2.dpkl', 'wb') as f:
        dpickle.dump(comment_proc, f)
    #Save the processed data
    np.save(OUTPUT_PATH/'py_t_code_vecs_v2.npy', t_code)
    np.save(OUTPUT_PATH/'py_t_comment_vecs_v2.npy', t_comment)


# Arrange data for modeling
from seq2seq_utils import load_decoder_inputs, load_encoder_inputs, load_text_processor
encoder_input_data, encoder_seq_length = load_encoder_inputs(OUTPUT_PATH/'py_t_code_vecs_v2.npy')
decoder_input_data, decoder_seq_length = load_decoder_inputs(OUTPUT_PATH/'py_t_comment_vecs_v2.npy')
num_encoder_tokens, enc_pp = load_text_processor(OUTPUT_PATH/'py_code_proc_v2.dpkl')
num_decoder_tokens, dec_pp = load_text_processor(OUTPUT_PATH/'py_comment_proc_v2.dpkl')



# Build Seq2Seq Model for summarizing code
from seq2seq_utils import build_seq2seq_model
seq2seq_Model = build_seq2seq_model(word_emb_dim=800,
                                    hidden_state_dim=1000,
                                    encoder_seq_len=encoder_seq_length,
                                    num_encoder_tokens=num_encoder_tokens,
                                    num_decoder_tokens=num_decoder_tokens)
seq2seq_Model.summary()


# Train Seq2Seq Model



