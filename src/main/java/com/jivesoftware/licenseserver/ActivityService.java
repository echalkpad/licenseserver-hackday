package com.jivesoftware.licenseserver;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.jivesoftware.activitystreams.v1.rest.ActivityRepresentation;
import com.jivesoftware.activitystreams.v1.rest.ActivityStreamRepresentation;
import com.jivesoftware.activitystreams.v1.services.ActivityStreamService;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static javax.ws.rs.core.Response.Status.Family.SUCCESSFUL;
import static javax.ws.rs.core.Response.Status.fromStatusCode;

@Singleton
public class ActivityService {

  private static final Logger LOG = LogManager.getLogger(ActivityService.class);

  public static final List<UserRequest> INCOMING_USER_REQUESTS = Lists.newLinkedList();

  private ActivityStreamService activityStreamService;
  private MessageAdapter messageAdapter;
  private ActivityStreamFactory activityStreamFactory;

  @Inject
  public ActivityService() {}

  public void publish(final String jiveUUID, long userId, String appUUID, LicenseServerMessage message) {
    final ActivityStreamRepresentation activityStream = activityStreamFactory.create();

    final List<ActivityRepresentation> list = new ArrayList<ActivityRepresentation>();
    list.add(messageAdapter.convert(message, userId));

    activityStream.setActivities(list);

    Response response = activityStreamService.postActivity(jiveUUID, appUUID, userId, userId, activityStream);

    if (LOG.isDebugEnabled()) {
      LOG.debug("status code [" + response.getStatus() + "], status family=[" +
          fromStatusCode(response.getStatus()).getFamily() +  "] response object[" + response + "]");
    }

    if(SUCCESSFUL != fromStatusCode(response.getStatus()).getFamily()) {
      throw new RuntimeException("Error posting the activity, status code [" + response.getStatus() + "], status family=[" +
          fromStatusCode(response.getStatus()).getFamily() + "] response object[" + response + "]");
    }
  }

  @Inject
  public void setActivityStreamServiceFactory(ActivityStreamServiceFactory factory) {
    ActivityStreamService activityStreamService = factory.build();
    this.activityStreamService = activityStreamService;
  }

  @Inject
  public void setMessageAdapter(MessageAdapter messageAdapter) {
    this.messageAdapter = messageAdapter;
  }

  @Inject
  public void setActivityStreamFactory(ActivityStreamFactory activityStreamFactory) {
    this.activityStreamFactory = activityStreamFactory;
  }
}
