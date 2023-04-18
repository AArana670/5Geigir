package com.example.a5geigir;

import com.example.a5geigir.db.Measurement;

public interface ReaderListener {

    void onNetworkUpdate(Measurement m);

}
