import sys
sys.path.append("../")

from typing import List
import logging, os

from lacivert.turkish_nltk.morphology import Morphology
from lacivert.turkish_nltk.tokenization import Tokenizer
from lacivert.helper.Helper import JVMHelper, InputHandler
from lacivert import properties

logging.basicConfig(level=logging.WARNING)

helper = JVMHelper()
helper.startJVM()

handler = InputHandler()
doc = handler.get_file_content_as_str(os.path.join("test_data", "aclik_oyunlari.txt"))


morphology = Morphology()
tokenizer = Tokenizer()

target_word_list: List[str] = ["gülmek", "bakmak", "kararmak"]

sentences = tokenizer.detect_sentences_boundaries(doc)
dis_res = morphology.disambiguate_sentences(sentences)



for idx, _ in enumerate(dis_res):
    print(f"{idx} -> {_}")
exit(1)

# print(morphology.change_stem("koştum", "bak"))
test_dis = [dis_res]
for s, t in [(source, target) for source in source_word_list for target in target_word_list]:
    print(f"Source word: {s}\tTarget word: {t}\tGenerated word: {morphology.change_stem(s, t)}")
