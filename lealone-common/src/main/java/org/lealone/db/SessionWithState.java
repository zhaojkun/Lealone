/*
 * Copyright 2004-2013 H2 Group. Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.lealone.db;

import java.util.ArrayList;

import org.lealone.common.util.New;
import org.lealone.db.result.Result;
import org.lealone.db.value.Value;

/**
 * The base class for both remote and embedded sessions.
 */
public abstract class SessionWithState implements SessionInterface {

    protected ArrayList<String> sessionState;
    protected boolean sessionStateChanged;
    private boolean sessionStateUpdating;

    /**
     * Re-create the session state using the stored sessionState list.
     */
    protected void recreateSessionState() {
        if (sessionState != null && sessionState.size() > 0) {
            sessionStateUpdating = true;
            try {
                for (String sql : sessionState) {
                    CommandInterface ci = prepareCommand(sql, Integer.MAX_VALUE);
                    ci.executeUpdate();
                }
            } finally {
                sessionStateUpdating = false;
                sessionStateChanged = false;
            }
        }
    }

    /**
     * Read the session state if necessary.
     */
    public void readSessionState() {
        if (!sessionStateChanged || sessionStateUpdating) {
            return;
        }
        sessionStateChanged = false;
        sessionState = New.arrayList();
        CommandInterface ci = prepareCommand("SELECT * FROM INFORMATION_SCHEMA.SESSION_STATE", Integer.MAX_VALUE);
        Result result = ci.executeQuery(0, false);
        while (result.next()) {
            Value[] row = result.currentRow();
            sessionState.add(row[1].getString());
        }
    }

}
