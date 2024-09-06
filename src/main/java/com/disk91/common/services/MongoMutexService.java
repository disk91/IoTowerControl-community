/*
 * Copyright (c) - Paul Pinault (aka disk91) - 2024.
 *
 *    Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 *    and associated documentation files (the "Software"), to deal in the Software without restriction,
 *    including without limitation the rights to use, copy, modify, merge, publish, distribute,
 *    sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 *    furnished to do so, subject to the following conditions:
 *
 *    The above copyright notice and this permission notice shall be included in all copies or
 *    substantial portions of the Software.
 *
 *    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 *    FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 *    OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
 *    IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.disk91.common.services;

import com.disk91.common.mdb.entities.MongoMutex;
import com.disk91.common.tools.Now;
import com.mongodb.client.result.UpdateResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MongoMutexService {
    private final Logger log        = LoggerFactory.getLogger(this.getClass());
    private final boolean verbose   = false;

    @Autowired
    protected MongoTemplate mongoTemplate;

    /**
     * Get the mutex. Create it if does not exists. Ensure it has not been frozen by a dead process.
     * Non blocking.
     * @param mutexId
     * @param identity
     * @param timeoutMs timeout in ms
     * @return true when reserved, false when not.
     */
    public boolean P(String mutexId, String identity,long timeoutMs) {
        // Verify if exists
        Query query;

        query = new Query();
        query.addCriteria(Criteria.where("mutexId").is(mutexId));
        List<MongoMutex> my = mongoTemplate.find(query,MongoMutex.class);
        if (my.isEmpty()) {
            // create it assuming no collision at this point
            log.debug("[common] MongoMutex Creating ({})", mutexId);
            MongoMutex _m = new MongoMutex();
            _m.setMutexId(mutexId);
            _m.setTaken(false);
            _m.setTimeout(timeoutMs);
            mongoTemplate.save(_m);
        } else if (my.size() > 1) {
            // problem
            log.warn("[common] MongoMutex Got multiple for id ({})", mutexId);
            for ( MongoMutex _m : my) {
                mongoTemplate.remove(_m);
            }
        } else {
            // check the validity of the mutex
            MongoMutex _m = my.getFirst();
            if ( _m.isTaken() ) {
                if ( (Now.NowUtcMs() - _m.getTakenAt()) > _m.getTimeout() ) {
                    // Mutex blocked for more than timeout minutes looks like frozen...
                    _m.setTaken(false);
                    mongoTemplate.save(_m);
                    log.warn("[common] MongoMutex ({}) reserved by ({}) since ({}) is now unlocked !", _m.getMutexId(), _m.getTakenBy(), _m.getTakenAt());
                }
            }
        }

        // try an update
        query = new Query();
        Criteria c = new Criteria();
        c.andOperator(
                Criteria.where("mutexId").is(mutexId),
                Criteria.where("taken").is(false));
        query.addCriteria(c);
        Update update = new Update();
        update.set("taken", true);
        UpdateResult _r = mongoTemplate.updateFirst(query,update,MongoMutex.class);
        if ( _r.getModifiedCount() > 0 )
        {
            // success ...
            // Update the information
            query = new Query();
            query.addCriteria(Criteria.where("mutexId").is(mutexId));
            MongoMutex _m = mongoTemplate.findOne(query,MongoMutex.class);
            _m.setTakenAt(Now.NowUtcMs());
            _m.setTakenBy(identity);
            _m.setTimeout(timeoutMs);
            mongoTemplate.save(_m);
            if ( verbose ) log.debug("[common] MongoMutex ({}) reserved by ({})", mutexId, identity);
            return true;
        } else {
            log.debug("[common] MongoMutex Failed to reserve Mutex ({}) by ({})", mutexId, identity);
            query = new Query();
            query.addCriteria(Criteria.where("mutexId").is(mutexId));
            MongoMutex _m = mongoTemplate.findOne(query,MongoMutex.class);
            if ( _m != null ) {
                log.info("[common] MongoMutex owned by {} since {}m", _m.getTakenBy(), (Now.NowUtcMs() - _m.getTakenAt()) / 60_000);
            }
            return false;
        }

    }

    /**
     * Release a mutex previously reserved
     * @param mutexId
     */
    public void V(String mutexId) {
        Query query = new Query();
        Criteria c = new Criteria();
        c.andOperator(
                Criteria.where("mutexId").is(mutexId),
                Criteria.where("taken").is(true));
        query.addCriteria(c);
        Update update = new Update();
        update.set("taken", false);
        UpdateResult _r = mongoTemplate.updateFirst(query,update,MongoMutex.class);
        if ( _r.getModifiedCount() == 0 ) {
            log.warn("[common] MongoMutex Try to release a Mutex already realeased ...({})",mutexId);
        } else if ( _r.getModifiedCount() > 1 ) {
            log.warn("[common] MongoMutex Found multiple Mutex");
        } else {
            if (verbose) log.debug("[common] MongoMutex ({}) released",mutexId);
        }
    }

    /**
     * Release a mutex previously reserved
     * @param mutexId
     */
    public void V(String mutexId, String identity) {
        Query query = new Query();
        Criteria c = new Criteria();
        c.andOperator(
                Criteria.where("mutexId").is(mutexId),
                Criteria.where("taken").is(true),
                Criteria.where("takenBy").is(identity));
        query.addCriteria(c);
        Update update = new Update();
        update.set("taken", false);
        UpdateResult _r = mongoTemplate.updateFirst(query,update,MongoMutex.class);
        if ( _r.getModifiedCount() == 0 ) {
            log.warn("[common] MongoMutex Try to release a Mutex already realeased ...({})",mutexId);
        } else if ( _r.getModifiedCount() > 1 ) {
            log.warn("[common] MongoMutex Found multiple Mutex");
        } else {
            if (verbose) log.debug("[common] MongoMutex ({}) released",mutexId);
        }
    }

    /**
     * search for a pending process for a given node
     * @param identity
     * @return
     */
    public boolean isAnyMutexReserved(String identity) {
        Query query;
        query = new Query();
        Criteria c = new Criteria();
        c.andOperator(
                Criteria.where("taken").is(true),
                Criteria.where("takenBy").is(identity)
        );
        query.addCriteria(c);
        MongoMutex _m = mongoTemplate.findOne(query,MongoMutex.class);
        if ( _m != null ) return true;
        return false;
    }

}
