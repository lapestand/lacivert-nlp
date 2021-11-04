from jpype import JClass, getDefaultJVMPath, startJVM, shutdownJVM, JString, isJVMStarted, java
from typing import List
import os
import logging
from lacivert import properties


class Morphology:
    
    
    NOUN = "Noun"
    VERB = "Verb"


    def __init__(self):
        
        Morphology: JClass = JClass('zemberek.morphology.TurkishMorphology')
        self.morphology: Morphology = Morphology.createWithDefaults()

        self.RootLexicon: JClass = JClass('zemberek.morphology.lexicon.RootLexicon')
        
        self.InformalAnalysisConverter: JClass = JClass('zemberek.morphology.analysis.InformalAnalysisConverter')
        
        self.AnalysisFormatters: JClass = JClass('zemberek.morphology.analysis.AnalysisFormatters')
        
        self.DictionaryItem: JClass = JClass('zemberek.morphology.lexicon.DictionaryItem')
        
        self.WordAnalysis: JClass = JClass('zemberek.morphology.analysis.WordAnalysis')
        
                
        logging.info("Morphology class initialized")

    def disambiguate(self, sentence):
        logging.info("lemmatization started")
        analysis: java.util.ArrayList = self.morphology.analyzeSentence(sentence)
        results: java.util.ArrayList = self.morphology.disambiguate(sentence, analysis).bestAnalysis()
        print("CHECK BELOW")
        res: List[str] = []

        for i, analysis in enumerate(results, start=1):
            # print(
            #     f'\nAnalysis {i}: {analysis}',
            #     f'\nAnalysis {i}: {type(analysis)}',
            #     f'\nPrimary POS {i}: {analysis.getPos()}'
            #     f'\nPrimary POS (Short Form) {i}: {analysis.getPos().shortForm}',
            #     f'\nMorphemes {i}: {analysis.getMorphemes()}'
            #     f'\nsurFaceForm {i}: {analysis.surfaceForm()}'
            #     f'\ngetLemmas {i}: {analysis.getLemmas()}'
            #     f'\ngetStemAndEnding {i}: {analysis.getStemAndEnding()}'
            #     f'\ngetDictionaryItem {i}: {analysis.getDictionaryItem()}'
            #     f'\ngetDictionaryItem {i}: {type(analysis.getDictionaryItem())}'
            #     f'\nisUnknown {i}: {analysis.isUnknown()}'
            # )
            res.append({
                "root": analysis.getLemmas()[0],
                "pos": analysis.getPos().shortForm,
                "dictForm": analysis.getDictionaryItem(),
                "dictFormStr": str(analysis.getDictionaryItem()),
                "unk": analysis.isUnknown(),
                "morphemes": analysis.getMorphemes(),
                "morphemesStr": str(analysis.getMorphemes())
                # f'{str(analysis.getLemmas()[0])}-{analysis.getPos().shortForm}'
            })


        return res
