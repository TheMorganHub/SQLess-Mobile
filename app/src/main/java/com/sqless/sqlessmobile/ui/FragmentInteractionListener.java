package com.sqless.sqlessmobile.ui;


import com.sqless.sqlessmobile.network.SQLConnectionManager;

public interface FragmentInteractionListener {

    void onInteraction(String title, SQLConnectionManager.ConnectionData data);
}
