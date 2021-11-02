package zemberek.embedding;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import zemberek.core.collections.IntFloatMap;
import zemberek.core.collections.UIntMap;
import zemberek.core.io.IOUtil;
import zemberek.core.logging.Log;
import zemberek.core.math.FloatArrays;
import zemberek.lm.LmVocabulary;

public class WordVectorLookup {

  private LmVocabulary vocabulary;
  private UIntMap<Vector> vectors;
  private int dimension;

  private WordVectorLookup(LmVocabulary vocabulary, UIntMap<Vector> vectors) {
    this.vocabulary = vocabulary;
    this.vectors = vectors;
    this.dimension = vectors.get(0).data.length;
  }

  private WordVectorLookup(LmVocabulary vocabulary, Vector[] vectorArray) {
    this.vocabulary = vocabulary;
    this.vectors = new UIntMap<>(vocabulary.size());
    for (Vector vector : vectorArray) {
      vectors.put(vector.wordIndex, vector);
    }
    this.dimension = vectors.get(0).data.length;
  }

  public static WordVectorLookup loadFromText(Path txtFile, boolean skipFirstLine)
      throws IOException {

    List<String> lines = Files.readAllLines(txtFile);
    // generate vocabulary
    LmVocabulary.Builder builder = new LmVocabulary.Builder();
    int lineCount = 0;
    for (String line : lines) {
      if (lineCount++ == 0 && skipFirstLine) { // skip first line.
        continue;
      }
      int index = line.indexOf(' ');
      String word = line.substring(0, index);
      builder.add(word);
    }
    LmVocabulary vocabulary = builder.generate();

    UIntMap<Vector> vectors = new UIntMap<>(lines.size());
    lineCount = 0;

    for (String line : lines) {
      if (lineCount++ == 0 && skipFirstLine) { // skip first line.
        continue;
      }
      line = line.trim();
      int index = line.indexOf(' ');
      String word = line.substring(0, index);
      String floats = line.substring(index + 1);
      float[] vector = FloatArrays.fromString(floats, " ");
      int wordIndex = vocabulary.indexOf(word);
      vectors.put(wordIndex, new Vector(wordIndex, vector));
    }
    return new WordVectorLookup(vocabulary, vectors);
  }

  public static WordVectorLookup loadFromBinaryFast(Path vectorFile, Path vocabularyFile)
      throws IOException {

    LmVocabulary vocabulary = LmVocabulary.loadFromBinary(vocabularyFile.toFile());

    int wordCount;
    int vectorDimension;
    try (DataInputStream dis = IOUtil.getDataInputStream(vectorFile)) {
      wordCount = dis.readInt();
      vectorDimension = dis.readInt();
    }

    RandomAccessFile aFile = new RandomAccessFile(vectorFile.toFile(), "r");
    FileChannel inChannel = aFile.getChannel();

    long start = 8, size;
    int blockSize = 4 + vectorDimension * 4;

    Vector[] vectors = new Vector[wordCount];

    int wordCounter = 0;
    int wordBlockSize = 100_000;

    while (wordCounter < wordCount) {

      if (wordCounter + wordBlockSize > wordCount) {
        wordBlockSize = (wordCount - wordCounter);
      }
      size = blockSize * wordBlockSize;

      MappedByteBuffer buffer = inChannel.map(FileChannel.MapMode.READ_ONLY, start, size);
      buffer.load();

      start += size;

      int blockCounter = 0;
      while (blockCounter < wordBlockSize) {
        int wordIndex = buffer.getInt();
        float[] data = new float[vectorDimension];
        buffer.asFloatBuffer().get(data);
        vectors[wordCounter] = new Vector(wordIndex, data);
        wordCounter++;
        blockCounter++;
        buffer.position(blockCounter * blockSize);
      }
    }
    return new WordVectorLookup(vocabulary, vectors);
  }

  public static WordVectorLookup loadFromBinary(Path vectorFile, Path vocabularyFile)
      throws IOException {

    LmVocabulary vocabulary = LmVocabulary.loadFromBinary(vocabularyFile.toFile());

    try (DataInputStream dis = IOUtil.getDataInputStream(vectorFile)) {
      int wordCount = dis.readInt();
      int vectorDimension = dis.readInt();
      Vector[] vectors = new Vector[wordCount];

      for(int  i = 0; i < wordCount ; i++) {
        int index = dis.readInt();
        if(index>wordCount || index<0) {
          throw new IllegalStateException("Bad word index " + index);
        }
        float[] vec = FloatArrays.deserializeRaw(dis, vectorDimension);
        vectors[i] = new Vector(index, vec);
      }
      return new WordVectorLookup(vocabulary, vectors);
    }
  }


  public Iterable<Vector> getVectors() {
    return vectors.getValues();
  }

  public void saveToFolder(Path out, String id) throws IOException {
    Files.createDirectories(out);
    Path vocab = out.resolve(id + ".vocab");
    vocabulary.saveBinary(vocab.toFile());
    Log.info("Vocabulary %s is written.", vocab);
    Path vecPath = out.resolve(id + ".vec.bin");
    try (DataOutputStream dos = IOUtil.getDataOutputStream(vecPath)) {
      dos.writeInt(vectors.size());
      dos.writeInt(dimension);
      for (Vector ve : vectors.getValuesSortedByKey()) {
        if(ve.wordIndex<0 || ve.wordIndex>vocabulary.size()) {
          throw new IllegalStateException("Negative index!");
        }
        dos.writeInt(ve.wordIndex);
        FloatArrays.serializeRaw(dos, ve.data);
      }
    }
    Log.info("Binary Vector file %s is written.", vecPath);

  }

  public Vector getVector(String word) {
    return vectors.get(vocabulary.indexOf(word));
  }

  public boolean containsWord(String word) {
    return vocabulary.contains(word);
  }

  public int getDimension() {
    return dimension;
  }

  public static class Vector {

    int wordIndex;
    float[] data;

    Vector(int wordIndex, float[] vector) {
      this.wordIndex = wordIndex;
      this.data = vector;
    }

    public int getWordIndex() {
      return wordIndex;
    }

    public float[] getData() {
      return data;
    }

    float c() {
      float sum = 0;
      for (float v : data) {
        sum += v * v;
      }
      return (float) Math.sqrt(sum);
    }
  }

  public static class DistanceMatcher {

    WordVectorLookup lookup;
    private IntFloatMap cMap;

    public DistanceMatcher(WordVectorLookup lookup) {
      this.lookup = lookup;
      this.cMap = new IntFloatMap(lookup.vectors.size());
      for (int i : lookup.vectors.getKeys()) {
        cMap.put(i, lookup.vectors.get(i).c());
      }
    }

    float cosDistance(Vector v1, Vector v2) {
      float sum = 0;
      for (int i = 0; i < v1.data.length; i++) {
        sum += (v1.data[i] * v2.data[i]);
      }
      return sum / (cMap.get(v1.wordIndex) * cMap.get(v1.wordIndex));
    }

    public List<WordDistances.Distance> nearestK(String word, int k) {
      if (!lookup.vocabulary.contains(word)) {
        return Collections.emptyList();
      }
      int index = lookup.vocabulary.indexOf(word);
      Vector v = lookup.vectors.get(index);

      PriorityQueue<WordDistances.Distance> queue = new PriorityQueue<>(k * 2);

      for (Vector other : lookup.vectors.getValues()) {
        // skip self.
        if (v == other) {
          continue;
        }
        float distance = cosDistance(v, other);
        if (queue.size() < k) {
          queue.add(
              new WordDistances.Distance(lookup.vocabulary.getWord(other.wordIndex), distance));
        } else {
          WordDistances.Distance weakest = queue.peek();
          if (weakest.distance < distance) {
            queue.remove();
            queue.add(
                new WordDistances.Distance(lookup.vocabulary.getWord(other.wordIndex), distance));
          }
        }
      }

      ArrayList<WordDistances.Distance> result = new ArrayList<>(queue);
      Collections.reverse(result);
      return result;
    }
  }
}
