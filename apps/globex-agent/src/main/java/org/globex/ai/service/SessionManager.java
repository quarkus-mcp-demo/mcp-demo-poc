package org.globex.ai.service;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import org.globex.ai.agent.AgentManager;
import org.globex.ai.agent.AgentRequest;
import org.globex.ai.agent.AgentResponse;
import org.globex.ai.model.SessionStatus;
import org.globex.ai.persistence.RequestSession;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class SessionManager {

    @Inject
    EntityManager em;

    @Inject
    AgentManager agentManager;

    public String handleRequest(String request, String userId) {

        Session session = resumeSessionFromDatabase(userId);

        AgentResponse response = session.getAgent().sendRequestToAgent(new AgentRequest(request, session.getThreadId(), session.getCheckpointId()));
        session.setCheckpointId(response.checkpointId());
        if (response.requiresRouting()) {
            if ((response.routingTarget() == null || response.routingTarget().isEmpty() || response.routingTarget().equals(AgentManager.ROUTING_AGENT))
                    && session.isSpecialistSession()) {
                resetConversationState(session);
            } else if ((response.routingTarget() != null && !response.routingTarget().isEmpty() && !response.routingTarget().equals(AgentManager.ROUTING_AGENT))
                    && session.isRoutingSession()) {
                resetConversationState(session);
                session.setAgentName(response.routingTarget());
                session.setAgent(agentManager.getAgent(response.routingTarget()));
                session.setThreadId(UUID.randomUUID().toString());
                updateSessionInDatabase(session);
                return handleRequest(response.userRequest(), userId);
            }
        }
        updateSessionInDatabase(session);
        return response.response();
    }

    public void clearConversation(String userId) {
        deleteRequestSession(userId);
    }

    Session resumeSessionFromDatabase(String userId) {
        RequestSession requestSession = loadSessionFromDatabase(userId);
        if (requestSession == null) {
            Log.infof("Creating initial session for user %s", userId);
            Session session = createInitialSession(userId);
            updateSessionInDatabase(session);
            return session;
        } else if (requestSession.getCurrentAgentId() == null || requestSession.getCurrentAgentId().isEmpty()
                || requestSession.getConversationThreadId() == null || requestSession.getConversationThreadId().isEmpty()) {
            Log.warnf("Session for user %s found but missing required fields", userId);
            Session session = new Session();
            session.setSessionId(requestSession.getSessionId());
            session.setUserId(userId);
            session = populateSession(session);
            updateSessionInDatabase(session);
            return session;
        } else {
            Session session = new Session();
            session.setSessionId(requestSession.getSessionId());
            session.setUserId(userId);
            session.setAgentName(requestSession.getCurrentAgentId());
            session.setAgent(agentManager.getAgent(requestSession.getCurrentAgentId()));
            session.setThreadId(requestSession.getConversationThreadId());
            session.setCheckpointId(requestSession.getConversationCheckpointId());
            Log.infof("Resumed existing session for user %s; session: %s; threadId: %s; agent: %s", userId, session.getSessionId(), session.getThreadId(), session.getAgentName());
            return session;
        }
    }

    Session createInitialSession(String userId) {
        Session session = new Session();
        session.setSessionId(UUID.randomUUID().toString());
        session.setUserId(userId);
        return populateSession(session);
    }

    Session populateSession(Session session) {
        session.setAgentName(AgentManager.ROUTING_AGENT);
        session.setAgent(agentManager.getAgent(AgentManager.ROUTING_AGENT));
        session.setThreadId(UUID.randomUUID().toString());
        return session;
    }

    @Transactional
    @SuppressWarnings("rawtypes")
    RequestSession loadSessionFromDatabase(String userId) {
        Query findRequestSessionBySessionId = em.createNamedQuery("RequestSession.findByUserId");
        findRequestSessionBySessionId.setParameter("userId", userId);
        List requestSessions = findRequestSessionBySessionId.getResultList();
        if (requestSessions.isEmpty()) {
            Log.infof("No response session found for userId: %s", userId);
            return null;
        }
        return (RequestSession)requestSessions.getFirst();
    }

    @Transactional
    void updateSessionInDatabase(Session session) {
        RequestSession requestSession = loadSessionFromDatabase(session.getUserId());
        if (requestSession == null) {
            RequestSession rs = new RequestSession();
            rs.setUser(session.getUserId());
            rs.setSessionId(session.getSessionId());
            rs.setCurrentAgentId(session.getAgentName());
            rs.setStatus(SessionStatus.ACTIVE);
            rs.setConversationThreadId(session.getThreadId());
            rs.setCreatedAt(Instant.now());
            rs.setUpdatedAt(Instant.now());
            em.persist(rs);
        } else {
            requestSession.setCurrentAgentId(session.getAgentName());
            requestSession.setConversationThreadId(session.getThreadId());
            requestSession.setConversationCheckpointId(session.getCheckpointId());
            requestSession.setSessionId(session.getSessionId());
            requestSession.setStatus(SessionStatus.ACTIVE);
            requestSession.setUpdatedAt(Instant.now());
        }
    }

    @Transactional
    void deleteRequestSession(String userId) {
        RequestSession requestSession = loadSessionFromDatabase(userId);
        if (requestSession == null) {
            return;
        }
        em.remove(requestSession);
    }

    void resetConversationState(Session session) {
        session.setAgent(null);
        session.setThreadId(null);
        session.setAgentName(null);
        session.setCheckpointId(null);
    }
}
