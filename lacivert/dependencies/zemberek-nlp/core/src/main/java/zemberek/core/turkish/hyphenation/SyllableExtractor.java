package zemberek.core.turkish.hyphenation;

import java.util.List;

public interface SyllableExtractor {

  /**
   * returns a list of Strings representing syllables for a given input.
   *
   * @param input input word.
   * @return list of syllables. if word cannot be parsed, an empty list is returned.
   */
  List<String> getSyllables(String input);
}
