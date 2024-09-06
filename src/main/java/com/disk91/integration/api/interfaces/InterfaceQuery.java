package com.disk91.integration.api.interfaces;

import com.disk91.common.api.interfaces.ActionResult;
import com.disk91.common.tools.Now;

public class InterfaceQuery {

    public enum QueryService {
        SERVICE_USERS,
        SERVICE_GROUPS,
    }

    public enum QueryState {
        STATE_PENDING, STATE_DONE, STATE_ERROR
    }

    public enum QueryType {
        TYPE_FIRE_AND_FORGET, TYPE_BROADCAST, TYPE_ASYNC, TYPE_SYNC
    }

    // ----------------------------------------------
    //  Common elements for all queries
    protected String queryId;                       // Uid of the message to link with response
    protected QueryService serviceNameSource;       // Service name for the source related to package (users)
    protected QueryService serviceNameDest;         // Service name for the destination (users)
    protected QueryType type;                       // Query type, fire & forget, broadcast, async, sync...
    protected int action;                           // Query action, value depends on services
    protected Object query;                         // Query parameters as an Object depending on oction
    protected ActionResult response;                // Result of the Query, sucess or error like Exception
    protected Object result;                        // Query response as an Object depending on action
    protected QueryState state;                     // Query state, STATE_PENDING, STATE_DONE, STATE_ERROR
                                                    // Query access request parallelism support
    protected long query_ts;                        // Query start time ref, structure creation in ns
    protected long response_ts;                     // Response time ref, in ns
    protected long query_ms;                        // Query timestamp in ms for timeout


    // ----------------------------------------------
    //  Message initialization
    public InterfaceQuery(QueryService serviceNameSource) {
        this.query_ts = Now.NanoTime();
        this.query_ms = Now.NowUtcMs();
        this.queryId = java.util.UUID.randomUUID().toString();
        this.serviceNameSource = serviceNameSource;
        this.state = QueryState.STATE_PENDING;
    }


    // ----------------------------------------------
    //  Manage state update
    private final static Object lock = new Object();

    public QueryState getState() {
        synchronized (lock) {
            return state;
        }
    }

    public void setStateDone() {
        synchronized (lock) {
            state = QueryState.STATE_DONE;
        }
    }

    public void setStateError() {
        synchronized (lock) {
            state = QueryState.STATE_ERROR;
        }
    }

    // ----------------------------------------------
    // Manage rest of the structure


    public String getQueryId() {
        return queryId;
    }

    public QueryService getServiceNameSource() {
        return serviceNameSource;
    }

    public void setServiceNameSource(QueryService serviceNameSource) {
        this.serviceNameSource = serviceNameSource;
    }

    public QueryService getServiceNameDest() {
        return serviceNameDest;
    }

    public void setServiceNameDest(QueryService serviceNameDest) {
        this.serviceNameDest = serviceNameDest;
    }

    public QueryType getType() {
        return type;
    }

    public void setType(QueryType type) {
        this.type = type;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public Object getQuery() {
        return query;
    }

    public void setQuery(Object query) {
        this.query = query;
    }

    public ActionResult getResponse() {
        return response;
    }

    public void setResponse(ActionResult response) {
        this.response = response;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public long getQuery_ts() {
        return query_ts;
    }

    public void setQuery_ts(long query_ts) {
        this.query_ts = query_ts;
    }

    public long getResponse_ts() {
        return response_ts;
    }

    public void setResponse_ts(long response_ts) {
        this.response_ts = response_ts;
    }

    public long getQuery_ms() {
        return query_ms;
    }

    public void setQuery_ms(long query_ms) {
        this.query_ms = query_ms;
    }
}
