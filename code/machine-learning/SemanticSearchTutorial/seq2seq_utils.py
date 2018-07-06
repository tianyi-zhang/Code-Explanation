from matplotlib import pyplot as plt
import tensorflow as tf
from keras import backend as K
from keras.models import Model
from keras.layers import Input, LSTM, GRU, Dense, Embedding, Bidirectional, BatchNormalization
from IPython.display import SVG, display
from keras.utils.vis_utils import model_to_dot
import logging
import numpy as np
import dill as dpickle
from annoy import AnnoyIndex
from tqdm import tqdm, tqdm_notebook
from random import random
from nltk.translate.bleu_score import corpus_bleu

def load_text_processor(fname='title_pp.dpkl'):
    # Load preprocessors from disk
    with open(fname, 'rb') as f:
        pp = dpickle.load(f)
    num_tokens = max(pp.id2token.keys()) + 1
    print(f'Size of vocabulary for {fname}: {num_tokens:,}')
    return num_tokens, pp

def load_decoder_inputs(decoder_np_vecs='train_title_vecs.npy'):
    vectorized_title = np.load(decoder_np_vecs)
    #Decoder target data is ahead by 1 time step from decoder input data
    decoder_input_data = vectorized_title[:, :-1]
    decoder_target_data = vectorized_title[:, 1:]
    print(f'Shape of decoder_input_data: {decoder_input_data.shape}')
    print(f'Shape of decoder_target_data: {decoder_target_data.shape}')
    return decoder_input_data, decoder_target_data

def load_encoder_inputs(encoder_np_vecs='train_body_vecs.npy'):
    vectorized_body = np.load(encoder_np_vecs)
    #Encoder input is the body of the issue text
    encoder_input_data = vectorized_body
    doc_length = encoder_input_data.shape[1]
    print(f'Shape of encoder_input_data: {encoder_input_data.shape}')
    return encoder_input_data, doc_length

def build_seq2seq_model(word_emb_dim,
                        hidden_state_dim,
                        encoder_seq_len,
                        num_encoder_tokens,
                        num_decoder_tokens):
    # Encoder model
    encoder_inputs = Input(shape=(encoder_seq_len,), name='Encoder-Input')
    x = Embedding(num_encoder_tokens, word_emb_dim, name='Body-Word-Embedding', mask_zero=False)(encoder_inputs)
    x = BatchNormalization(name='Encoder-Batchnorm-1')(x)
    _, state_h = GRU(hidden_state_dim, return_state=True, name='Encoder-Last-GRU', dropout=.5)(x)
    encoder_model = Model(inputs=encoder_inputs, outputs=state_h, name='Encoder-Model')
    seq2seq_encoder_out = encoder_model(encoder_inputs)

    # Decoder model
    decoder_inputs = Input(shape=(None,), name='Decoder-Input') #teacher forcing
    dec_emb = Embedding(num_decoder_tokens, word_emb_dim, name='Decoder-Word-Embedding', mask_zero=False)(decoder_inputs)
    dec_bn = BatchNormalization(name='Decoder-Batchnorm-1')(dec_emb)
    decoder_gru = GRU(hidden_state_dim, return_state=True, return_sequences=True, name='Decoder-GRU', dropout=.5)
    decoder_gru_output, _ = decoder_gru(dec_bn, initial_state=seq2seq_encoder_out)
    x = BatchNormalization(name='Decoder-Batchnorm-2')(decoder_gru_output)

    decoder_dense = Dense(num_decoder_tokens, activation='softmax', name='Final_Output_Dense')
    decoder_outputs = decoder_dense(x)

    #Seq2Seq model
    seq2seq_Model = Model([encoder_inputs, decoder_inputs], decoder_outputs)
    return seq2seq_Model