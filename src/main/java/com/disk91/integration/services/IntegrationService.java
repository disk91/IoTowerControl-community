package com.disk91.integration.services;

import com.disk91.common.tools.Now;
import com.disk91.integration.api.interfaces.InterfaceQuery;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.ArrayList;
import java.util.HashMap;

public class IntegrationService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private HashMap<String, InterfaceQuery> _queries = new HashMap<>();

    private long maxQueryDuration;

    @PostConstruct
    public void initIntegration() {
        log.debug("[integration] Init");
        // max query lue depuis le fichier de conf
    }

    public InterfaceQuery requestSync(InterfaceQuery query) {
        log.debug("[integration] Query");
        // route the query and send the query (call the response function of send to the right service)
        // mqtt or http
        // response is the the service function corresponding, to here we have a big matrix ...
        // mais en gros on doit envoyer ca dans un processeur de requetes par service pour que ce soit plus logique
        // qui va envoyer sa reponse sur response...

        this._queries.put(query.getQueryId(), query);

        // wait for the response to be updated
        while ( query.getState() == InterfaceQuery.QueryState.STATE_PENDING ) {
            Tools.sleep(1);

        }
        return query;
    }

    public void response(InterfaceQuery query) {
        query.setResponse_ts(Now.NanoTime());
        query.setStateDone();
        log.debug("[integration] Response {} us", (query.getResponse_ts() - query.getQuery_ts())/1000);
        if ( this._queries.get(query.getQueryId()) == null ) {
            log.warn("[integration] Response not found (late) {} ms", Now.NowUtcMs() - query.getQuery_ms());
        }
        this._queries.remove(query.getQueryId());
    }

    @Scheduled(fixedRate = 1000)
    void garbage() {
        // remove all the queries that are too old
        long now = Now.NowUtcMs();
        ArrayList<String> toRemove = new ArrayList<>();
        this._queries.forEach((k,v) -> {
            if ( now - v.getQuery_ms() > this.maxQueryDuration ) {
                log.debug("[integration] Query {} timeout", v.getQueryId());
                v.setStateError();
                toRemove.add(k);
            }
        });
        toRemove.forEach(k -> this._queries.remove(k));
    }



}
