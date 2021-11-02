package zemberek.core.embeddings;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Locale;
import java.util.Random;
import zemberek.core.math.FloatArrays;

class Matrix {

  int m_;
  int n_;
  float[][] data_;

  private Matrix(int m_, int n_, float[][] data_) {
    this.m_ = m_;
    this.n_ = n_;
    this.data_ = data_;
  }

  private Matrix(int m_, int n_, float[] data_) {
    this.m_ = m_;
    this.n_ = n_;
    this.data_ = new float[m_][n_];
    for (int i = 0; i < m_; i++) {
      System.arraycopy(data_, i * n_, this.data_[i], 0, n_);
    }
  }


  /**
   * Generates a matrix that hs n_ columns and m_ rows.
   */
  Matrix(int m_, int n_) {
    this.m_ = m_;
    this.n_ = n_;
    this.data_ = new float[m_][n_];
    for (int i = 0; i < m_; i++) {
      this.data_[i] = new float[n_];
    }
  }

  public static final Matrix EMPTY = new Matrix(0,0, new float[0]);

  Matrix copy() {
    return new Matrix(m_, n_, FloatArrays.clone2D(data_));
  }

  float[] getData1D() {
    return FloatArrays.to1D(data_);
  }

  void set(int row, int col, float val) {
    data_[row][col] = val;
  }


  /**
   * loads the values from binary stream [dis] and instantiates the matrix.
   */
  static Matrix load(DataInputStream dis) throws IOException {
    int m_ = dis.readInt();
    int n_ = dis.readInt();
    float[][] data = new float[m_][n_];

    int blockSize = n_ * 4;

    int block = 100_000 * blockSize;
    long totalByte = (long) m_ * blockSize;
    if (block > totalByte) {
      block = (int) totalByte;
    }
    int start = 0;
    int end = block / blockSize;
    int blockCounter = 1;
    while (start < m_) {
      byte[] b = new byte[block];
      dis.readFully(b);
      ByteBuffer buffer = ByteBuffer.wrap(b);
      FloatBuffer fb = buffer.asFloatBuffer();
      float[] tmp = new float[block / 4];
      fb.get(tmp);

      for (int k = 0; k < tmp.length / n_; k++) {
        System.arraycopy(tmp, k * n_, data[k + start], 0, n_);
      }
      blockCounter++;
      start = end;
      end = (block / blockSize) * blockCounter;
      if (end > m_) {
        end = m_;
        block = (end - start) * blockSize;
      }
    }
    return new Matrix(m_, n_, data);
  }

  /**
   * Fills the Matrix with uniform random numbers in [-a a] range.
   */
  void uniform(float a) {
    Random random = new Random(1);
    for (int i = 0; i < m_; i++) {
      for (int j = 0; j < n_; j++) {
        float v = (float) (random.nextDouble() * 2 * a - a);
        data_[i][j] = v;
      }
    }
  }

  float at(int i, int j) {
    return data_[i][j];
  }


  /**
   * Sums the [a]*values of Vector [vec] to the [i].th row of the Matrix.
   */
  void addRow(Vector vec, int i, float a) {
    for (int j = 0; j < n_; j++) {
      data_[i][j] += a * vec.data_[j];
    }
  }

  /**
   * Calculates dot product of Vector [vec] and [i]th row. If locks are enabled, access to the row
   * is thread safe.
   */
  float dotRow(Vector vec, int i) {
    assert (i >= 0);
    assert (i < m_);
    assert (vec.size() == n_);
    float d = 0.0f;
    for (int j = 0; j < n_; j++) {
      d += data_[i][j] * vec.data_[j];
    }
    return d;
  }

  void multiplyRow(Vector nums) {
    multiplyRow(nums, 0, -1);
  }

  /**
   * Multiplies values in rows of the matrix with values of `nums`. nums should have a value for
   * each row to be multiplied. If value in `nums` is zero, no multiplication is applied for the
   * row.
   *
   * @param nums values to multiply.
   * @param ib begin row index.
   * @param ie end row index. if -1, it is assumed row count `m_`
   */
  void multiplyRow(Vector nums, int ib, int ie) {
    if (ie == -1) {
      ie = m_;
    }
    assert (ie <= nums.size());
    for (int i = ib; i < ie; i++) {
      float n = nums.data_[i - ib];
      if (n != 0) {
        for (int j = 0; j < n_; j++) {
          data_[i][j] *= n;
        }
      }
    }
  }

  void divideRow(Vector denoms) {
    divideRow(denoms, 0, -1);
  }

  /**
   * Divides values in rows of the matrix to values of `nums`. nums should have a value for each row
   * to be multiplied. If value in `nums` is zero, no division is applied for the row.
   *
   * @param denoms denominator values.
   * @param ib begin row index.
   * @param ie end row index. if -1, it is assumed row count `m_`
   */
  void divideRow(Vector denoms, int ib, int ie) {
    if (ie == -1) {
      ie = m_;
    }
    assert (ie <= denoms.size());
    for (int i = ib; i < ie; i++) {
      float n = denoms.data_[i - ib];
      if (n != 0) {
        for (int j = 0; j < n_; j++) {
          data_[i][j] /= n;
        }
      }
    }
  }

  /**
   * Calculates L2 Norm value of a row. Which is the Square root of  sum of squares of all row
   * values.
   *
   * @param i row index.
   * @return Square root of  sum of squares of all row values.
   */
  float l2NormRow(int i) {
    float norm = 0f;
    for (int j = 0; j < n_; j++) {
      float v = data_[i][j];
      norm += v * v;
    }
    return (float) Math.sqrt(norm);
  }

  /**
   * Fills the `norms` vector with l2 norm values of the rows.
   *
   * @param norms norm vector to fill.
   */
  void l2NormRow(Vector norms) {
    assert (norms.size() == m_);
    for (int i = 0; i < m_; i++) {
      norms.data_[i] = l2NormRow(i);
    }
  }

  /**
   * Saves values to binary stream [dos]
   */
  void save(DataOutputStream dos) throws IOException {
    dos.writeInt(m_);
    dos.writeInt(n_);

    int blockSize = n_ * 4;

    int block = 100_000 * blockSize;
    long totalByte = (long) m_ * blockSize;
    if (block > totalByte) {
      block = (int) totalByte;
    }
    int start = 0;
    int end = block / blockSize;
    int blockCounter = 1;
    while (start < m_) {
      byte[] b = new byte[block];
      ByteBuffer buffer = ByteBuffer.wrap(b);
      FloatBuffer fb = buffer.asFloatBuffer();
      for (int i = start; i < end; i++) {
        fb.put(data_[i]);
      }
      dos.write(b);
      blockCounter++;
      start = end;
      end = (block / blockSize) * blockCounter;
      if (end > m_) {
        end = m_;
        block = (end - start) * blockSize;
      }
    }
  }

  void printRow(String s, int i, int amount) {
    int n = amount > n_ ? n_ : amount;
    System.out.print(s + "[" + i + "] = ");
    for (int k = 0; k < n; k++) {
      System.out.print(String.format(Locale.ENGLISH,"%.4f ", data_[i][k]));
    }
    System.out.println();
  }

}
