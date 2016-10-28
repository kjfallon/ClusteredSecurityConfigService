package edu.syr.eecs.cis.cscs.entities.statemachine;
/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

import io.atomix.copycat.server.Commit;
import io.atomix.copycat.server.StateMachine;
import io.atomix.copycat.server.StateMachineExecutor;

/**
 * Value state machine.
 *
 * @author <a href="http://github.com/kuujo>Jordan Halterman</a>
 */

public class ValueStateMachine extends StateMachine {
    private Commit<ValueSetCommand> value;

    @Override
    protected void configure(StateMachineExecutor executor) {
        executor.register(ValueSetCommand.class, this::set);
        executor.register(ValueGetQuery.class, this::get);
        executor.register(ValueDeleteCommand.class, this::delete);
    }

    /**
     * Sets the value.
     */
    private Object set(Commit<ValueSetCommand> commit) {
        try {
            Commit<ValueSetCommand> previous = value;
            value = commit;
            if (previous != null) {
                Object result = previous.operation().value();
                previous.close();
                return result;
            }
            return null;
        } catch (Exception e) {
            commit.close();
            throw e;
        }
    }

    /**
     * Gets the value.
     */
    private Object get(Commit<ValueGetQuery> commit) {
        try {
            return value != null ? value.operation().value() : null;
        } finally {
            commit.close();
        }
    }

    /**
     * Deletes the value.
     */
    private void delete(Commit<ValueDeleteCommand> commit) {
        try {
            if (value != null) {
                value.close();
                value = null;
            }
        } finally {
            commit.close();
        }
    }

}