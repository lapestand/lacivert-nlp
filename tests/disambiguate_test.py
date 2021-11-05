import sys
sys.path.append("../")

from lacivert.turkish_nltk.Morphology import Morphology
from lacivert.helper.Helper import JVMHelper
import logging
from lacivert import properties

logging.basicConfig(level=logging.INFO)

helper = JVMHelper()
helper.startJVM()


sentence: str = "Açlık Oyunları (İngilizce özgün adıyla The Hunger Games), Amerikalı yazar Suzanne Collins'in 2008'de yayımlanan distopik macera türündeki romanıdır."
morphology = Morphology()
morphology.disambiguate(sentence)

