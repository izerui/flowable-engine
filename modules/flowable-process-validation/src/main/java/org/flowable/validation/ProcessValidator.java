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
package org.flowable.validation;

import java.util.List;

import org.flowable.bpmn.model.BpmnModel;
import org.flowable.validation.validator.ValidatorSet;

/**
 * Validates a process definition against the rules of the Activiti engine to be executable
 * 
 * @author jbarrez
 */
public interface ProcessValidator {

  /**
   * Validates the provided {@link BpmnModel} and returns a list of all {@link ValidationError} occurrences found.
   */
  List<ValidationError> validate(BpmnModel bpmnModel);

  /**
   * Returns the {@link ValidatorSet} instances for this process validator. Useful if some validation rules need to be disabled.
   */
  List<ValidatorSet> getValidatorSets();

}
