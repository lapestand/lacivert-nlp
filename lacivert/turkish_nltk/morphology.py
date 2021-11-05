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


    def disambiguate_doc(self, doc): pass


    def change_stem(self, source_word: str, target_word: str) -> str:
        logging.debug(f"Stem changing for '{target_word}' using '{source_word}' as source.")
        new_stem: self.DictionaryItem = (
            self.morphology.getLexicon().getMatchingItems(target_word).get(0)
        )

        logging.debug(f"Lexicon for {target_word} is: {self.morphology.getLexicon().getMatchingItems(target_word).get(0)}")
        logging.debug(f"Dict Item of above lexicon {new_stem}")
        

        results: self.WordAnalysis = self.morphology.analyze(JString(source_word))

        # print("--------------------------------")
        # print(f"Word Analysis result of {source_word}")
        # print(results)
        # print(type(results))
        # print("--------------------------------")

        for result in results:
            # print(result.getMorphemes())
            generated: java.util.ArrayList = (
                self.morphology.getWordGenerator().generate(
                    new_stem, result.getMorphemes()
                )
            )
            # for gen_word in generated:
            #     print(
            #         f'\nInput Analysis: {str(result.formatLong())}'
            #         f'\nAfter Stem Change, Word: {str(gen_word.surface)}'
            #         '\nAfter Stem Change, Analysis:'
            #         f'{str(gen_word.analysis.formatLong())}'
            #     )
        logging.info(f"{len(generated)} {'word' if len(generated) == 1 else 'words'} generated.")
        
        return None if len(generated) == 0 else str(generated[0].surface)

    
    def change_stem_of_analysed_word(self, source_word, target_word):

        new_stem: self.DictionaryItem = (
            self.morphology.getLexicon().getMatchingItems(target_word).get(0)
        )

        logging.debug(f"Lexicon for {target_word} is: {self.morphology.getLexicon().getMatchingItems(target_word).get(0)}")
        logging.debug(f"Dict Item of above lexicon {new_stem}")

        # print(type(source_word["morphemes"]))
        generated: java.util.ArrayList = (
            self.morphology.getWordGenerator().generate(
                new_stem, source_word["morphemes"]
            )
        )

        
        # for gen_word in generated:
        #     print(
        #         f'\nInput Analysis: {source_word["dictFormStr"]}'
        #         f'\nAfter Stem Change, Word: {str(gen_word.surface)}'
        #         '\nAfter Stem Change, Analysis:'
        #         f'{str(gen_word.analysis.formatLong())}'
        #     )
        
        logging.info(f"{len(generated)} {'word' if len(generated) == 1 else 'words'} generated.")
        
        return None if len(generated) == 0 else str(generated[0].surface)
