package zemberek.core.turkish;

import com.google.common.base.Preconditions;
import java.util.List;
import java.util.Objects;
import zemberek.core.io.Strings;
import zemberek.core.text.TextUtil;

/**
 * Represents a word splitted as stem and ending. if there is only stem, ending is empty String ""
 */
public class StemAndEnding {

  public final String stem;
  public final String ending;

  public StemAndEnding(String stem, String ending) {
    if (Strings.hasText(stem)) {
      Preconditions.checkArgument(Strings.hasText(stem), "Stem needs to have text");
    }
    if (!Strings.hasText(ending)) {
      ending = "";
    }
    this.stem = stem;
    this.ending = ending;
  }

  public static StemAndEnding fromSpaceSepareted(String input) {
    List<String> splitResult = TextUtil.SPACE_SPLITTER.splitToList(input);
    if (splitResult.size() == 1) {
      return new StemAndEnding(splitResult.get(0), "");
    } else if (splitResult.size() == 2) {
      return new StemAndEnding(splitResult.get(0), splitResult.get(1));
    }
    throw new IllegalArgumentException("Input contains more than two words" + input);
  }

  @Override
  public String toString() {
    return stem + "-" + ending;
  }

  public String concat() {
    return stem + ending;
  }

  public boolean hasEnding() {
    return ending.length() > 0;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    StemAndEnding that = (StemAndEnding) o;

    if (!Objects.equals(ending, that.ending)) {
      return false;
    }
    if (!Objects.equals(stem, that.stem)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = stem != null ? stem.hashCode() : 0;
    result = 31 * result + (ending != null ? ending.hashCode() : 0);
    return result;
  }
}
