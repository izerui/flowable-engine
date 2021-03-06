/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flowable.engine.impl.bpmn.behavior;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.model.Signal;
import org.flowable.bpmn.model.SignalEventDefinition;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.Expression;
import org.flowable.engine.delegate.event.FlowableEngineEventType;
import org.flowable.engine.delegate.event.impl.FlowableEventBuilder;
import org.flowable.engine.history.DeleteReason;
import org.flowable.engine.impl.context.Context;
import org.flowable.engine.impl.interceptor.CommandContext;
import org.flowable.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.flowable.engine.impl.persistence.entity.EventSubscriptionEntityManager;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.flowable.engine.impl.persistence.entity.SignalEventSubscriptionEntity;

public class IntermediateCatchSignalEventActivityBehavior extends IntermediateCatchEventActivityBehavior {

  private static final long serialVersionUID = 1L;

  protected SignalEventDefinition signalEventDefinition;
  protected Signal signal;

  public IntermediateCatchSignalEventActivityBehavior(SignalEventDefinition signalEventDefinition, Signal signal) {
    this.signalEventDefinition = signalEventDefinition;
    this.signal = signal;
  }

  public void execute(DelegateExecution execution) {
    CommandContext commandContext = Context.getCommandContext();
    ExecutionEntity executionEntity = (ExecutionEntity) execution;
    
    String signalName = null;
    if (StringUtils.isNotEmpty(signalEventDefinition.getSignalRef())) {
      signalName = signalEventDefinition.getSignalRef();
    } else {
      Expression signalExpression = commandContext.getProcessEngineConfiguration().getExpressionManager()
          .createExpression(signalEventDefinition.getSignalExpression());
      signalName = signalExpression.getValue(execution).toString();
    }
    
    commandContext.getEventSubscriptionEntityManager().insertSignalEvent(signalName, signal, executionEntity);
    
    if (commandContext.getProcessEngineConfiguration().getEventDispatcher().isEnabled()) {
      commandContext.getProcessEngineConfiguration().getEventDispatcher()
              .dispatchEvent(FlowableEventBuilder.createSignalEvent(FlowableEngineEventType.ACTIVITY_SIGNAL_WAITING, executionEntity.getActivityId(), signalName,
                      null, executionEntity.getId(), executionEntity.getProcessInstanceId(), executionEntity.getProcessDefinitionId()));
    }
  }

  @Override
  public void trigger(DelegateExecution execution, String triggerName, Object triggerData) {
    ExecutionEntity executionEntity = deleteSignalEventSubscription(execution);
    leaveIntermediateCatchEvent(executionEntity);
  }
  
  @Override
  public void eventCancelledByEventGateway(DelegateExecution execution) {
    deleteSignalEventSubscription(execution);
    Context.getCommandContext().getExecutionEntityManager().deleteExecutionAndRelatedData((ExecutionEntity) execution, 
        DeleteReason.EVENT_BASED_GATEWAY_CANCEL, false);
  }

  protected ExecutionEntity deleteSignalEventSubscription(DelegateExecution execution) {
    ExecutionEntity executionEntity = (ExecutionEntity) execution;

    String eventName = null;
    if (signal != null) {
      eventName = signal.getName();
    } else {
      eventName = signalEventDefinition.getSignalRef();
    }

    EventSubscriptionEntityManager eventSubscriptionEntityManager = Context.getCommandContext().getEventSubscriptionEntityManager();
    List<EventSubscriptionEntity> eventSubscriptions = executionEntity.getEventSubscriptions();
    for (EventSubscriptionEntity eventSubscription : eventSubscriptions) {
      if (eventSubscription instanceof SignalEventSubscriptionEntity && eventSubscription.getEventName().equals(eventName)) {

        eventSubscriptionEntityManager.delete(eventSubscription);
      }
    }
    return executionEntity;
  }
}
