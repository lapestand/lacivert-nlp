from jpype import JClass, getDefaultJVMPath, startJVM, shutdownJVM, JString, isJVMStarted, java
from typing import List
import os
import logging
import properties



class Tokenizer:


    def __init__(self):
        TurkishSentenceExtractor: JClass = JClass('zemberek.tokenization.TurkishSentenceExtractor')
        self.extractor: TurkishSentenceExtractor = TurkishSentenceExtractor.DEFAULT
        
        TurkishTokenizer: JClass = JClass('zemberek.tokenization.TurkishTokenizer')
        self.tokenizer: TurkishTokenizer = TurkishTokenizer.DEFAULT


        self.doubleQuoteIgnoreExtractor = TurkishSentenceExtractor.builder().doNotSplitInDoubleQuotes().build()

    def tokenize_sentence(self): pass


    def detect_sentences_boundaries(self, doc):
        return [str(_) for _ in self.doubleQuoteIgnoreExtractor.fromDocument(doc)]