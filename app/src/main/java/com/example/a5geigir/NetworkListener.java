package com.example.a5geigir;

import com.example.a5geigir.db.Measurement;
import com.example.a5geigir.db.Signal;

public interface NetworkListener {

    void onNetworkUpdate(Measurement m);

}
