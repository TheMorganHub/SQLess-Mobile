package com.sqless.sqlessmobile.sqlobjects;

public interface SQLCallable {

    String getCallStatement();
    
    String prepareParameters();
}
