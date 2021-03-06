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
package org.flowable.idm.api.event;

import org.flowable.engine.common.api.delegate.event.FlowableEvent;

/**
 * An event related to group memberships.
 * 
 * @author Tijs Rademakers
 */
public interface FlowableIdmMembershipEvent extends FlowableEvent {

  /**
   * @return related user. Returns null, if not related to a single user but rather to all members of the group.
   */
  String getUserId();

  /**
   * @return related group
   */
  String getGroupId();
}
