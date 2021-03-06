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
package org.flowable.admin.app.rest.client;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.flowable.admin.domain.EndpointType;
import org.flowable.admin.domain.ServerConfig;
import org.flowable.admin.service.engine.FormDeploymentService;
import org.flowable.admin.service.engine.exception.FlowableServiceException;
import org.flowable.app.service.exception.BadRequestException;
import org.flowable.app.service.exception.InternalServerErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author Yvo Swillens
 */
@RestController
@RequestMapping("/rest/admin/form-deployments")
public class FormDeploymentsClientResource extends AbstractClientResource {

  private static final Logger logger = LoggerFactory.getLogger(FormDeploymentsClientResource.class);

  @Autowired
  protected FormDeploymentService clientService;

  /**
   * GET /rest/admin/form-deployments -> get a list of form deployments.
   */
  @RequestMapping(method = RequestMethod.GET, produces = "application/json")
  public JsonNode listDeployments(HttpServletRequest request) {
    logger.debug("REST request to get a list of form deployments");

    JsonNode resultNode = null;
    ServerConfig serverConfig = retrieveServerConfig(EndpointType.FORM);
    Map<String, String[]> parameterMap = getRequestParametersWithoutServerId(request);

    try {
      resultNode = clientService.listDeployments(serverConfig, parameterMap);

    } catch (FlowableServiceException e) {
      logger.error("Error getting form deployments", e);
      throw new BadRequestException(e.getMessage());
    }

    return resultNode;
  }

  /**
   * POST /rest/admin/form-deployments: upload a form deployment
   */
  @RequestMapping(method = RequestMethod.POST, produces = "application/json")
  public JsonNode handleFileUpload(HttpServletRequest request, @RequestParam("file") MultipartFile file) {
    if (!file.isEmpty()) {
      try {
        ServerConfig serverConfig = retrieveServerConfig(EndpointType.FORM);
        String fileName = file.getOriginalFilename();
        if (fileName != null && (fileName.endsWith(".form") || fileName.endsWith(".xml"))) {

          return clientService.uploadDeployment(serverConfig, fileName, file.getInputStream());

        } else {
          logger.error("Invalid form deployment file name {}", fileName);
          throw new BadRequestException("Invalid file name");
        }

      } catch (IOException e) {
        logger.error("Error deploying form upload", e);
        throw new InternalServerErrorException("Could not deploy file: " + e.getMessage());
      }

    } else {
      logger.error("No form deployment file found in request");
      throw new BadRequestException("No file found in POST body");
    }
  }

}
