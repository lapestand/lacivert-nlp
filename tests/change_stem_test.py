import sys
from typing import List
sys.path.append("../")

from lacivert.turkish_nltk.morphology import Morphology
from lacivert.helper.Helper import JVMHelper
import logging

logging.basicConfig(level=logging.WARNING)

jvm_helper = JVMHelper()
jvm_helper.startJVM()

source_word_list: List[str] = ["koştum", "yüzdüm", "yaptım"]
target_word_list: List[str] = ["gülmek", "bakmak", "kararmak"]

morphology = Morphology()

# print(morphology.change_stem("koştum", "bak"))

for s, t in [(source, target) for source in source_word_list for target in target_word_list]:
    print(f"Source word: {s}\tTarget word: {t}\tGenerated word: {morphology.change_stem(s, t)}")
