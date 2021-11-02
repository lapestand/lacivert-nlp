package zemberek.core.embeddings;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

class QMatrix {

  ProductQuantizer pq_;
  ProductQuantizer npq_;

  byte[] codes_;
  byte[] norm_codes_;

  boolean qnorm_;

  int m_;
  int n_;

  int codesize_;

  public QMatrix() {
    m_ = 0;
    n_ = 0;
    codesize_ = 0;
  }

  public QMatrix(Matrix mat, int dsub, boolean qnorm) {
    qnorm_ = qnorm;
    m_ = mat.m_;
    n_ = mat.n_;
    codesize_ = m_ * ((n_ + dsub - 1) / dsub);
    if(codesize_>0) {
      codes_ = new byte[codesize_];
    } else {
      throw new IllegalStateException("Code size must be a positive number.");
    }
    pq_ = new ProductQuantizer(n_, dsub);
    if (qnorm_) {
      norm_codes_ = new byte[m_];
      npq_ = new ProductQuantizer(1, 1);
    }
    quantize(mat);
  }


  void quantizeNorm(Vector norms) {
    assert (qnorm_);
    assert (norms.size() == m_);
    float[] dataptr = norms.data_;
    npq_.train(m_, dataptr);
    ProductQuantizer.FArray fArray = new ProductQuantizer.FArray(dataptr);
    ProductQuantizer.BArray bArray = new ProductQuantizer.BArray(norm_codes_);
    npq_.compute_codes(fArray, bArray, m_);
  }

  void quantize(Matrix matrix) {
    assert (n_ == matrix.n_);
    assert (m_ == matrix.m_);
    if (qnorm_) {
      Vector norms = new Vector(matrix.m_);
      matrix.l2NormRow(norms);
      matrix.divideRow(norms);
      quantizeNorm(norms);
    }
    float[] data1D = matrix.getData1D();
    pq_.train(m_, data1D);
    ProductQuantizer.FArray fArray = new ProductQuantizer.FArray(data1D);
    ProductQuantizer.BArray bArray = new ProductQuantizer.BArray(codes_);
    pq_.compute_codes(fArray, bArray, m_);
  }

  void addToVector(Vector x, int t) {
    float norm = 1;
    if (qnorm_) {
      norm = npq_.get_centroids(0, norm_codes_[t]).get(0);
    }
    pq_.addcode(x, new ProductQuantizer.BArray(codes_), t, norm);
  }

  float dotRow(Vector vec, int i) {
    assert (i >= 0);
    assert (i < m_);
    assert (vec.size() == n_);
    float norm = 1;
    if (qnorm_) {
      norm = npq_.get_centroids(0, norm_codes_[i]).get(0);
    }
    return pq_.mulcode(vec, new ProductQuantizer.BArray(codes_), i, norm);
  }

  int getM() {
    return m_;
  }

  int getN() {
    return n_;
  }

  void save(DataOutputStream out) throws IOException {
    out.writeBoolean(qnorm_);
    out.writeInt(m_);
    out.writeInt(n_);
    out.writeInt(codesize_);
    out.write(codes_);
    pq_.save(out);
    if (qnorm_) {
      out.write(norm_codes_);
      npq_.save(out);
    }
  }

  void load(DataInputStream in) throws IOException {
    qnorm_ = in.readBoolean();
    m_ = in.readInt();
    n_ = in.readInt();
    codesize_ = in.readInt();
    codes_ = new byte[codesize_];
    in.readFully(codes_);
    pq_ = new ProductQuantizer();
    pq_.load(in);
    if (qnorm_) {
      byte[] normCodesData = new byte[m_];
      in.readFully(normCodesData);
      npq_ = new ProductQuantizer();
      npq_.load(in);
    }
  }
}
