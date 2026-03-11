package org.globex.ai.persistence;

import jakarta.persistence.*;
import org.globex.ai.model.SessionStatus;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.type.PostgreSQLEnumJdbcType;

import java.time.Instant;

@Entity
@Table(name = "request_sessions")
@SequenceGenerator(name="RequestSessionsSeq", sequenceName="request_sessions_seq", allocationSize = 1)
@NamedQueries({
        @NamedQuery(name = "RequestSession.findBySessionId", query = "from RequestSession where sessionId = :sessionId"),
        @NamedQuery(name = "RequestSession.findBySessionIdAndStatus", query = "from RequestSession where sessionId = :sessionId and status = :status"),
        @NamedQuery(name = "RequestSession.findByUserId", query = "from RequestSession where user = :user")
})
public class RequestSession {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator="RequestSessionsSeq")
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "session_id", nullable = false, unique = true, length = 36)
    private String sessionId;

    @Column(name = "user_id", nullable = false, unique = true)
    private String user;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private SessionStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "current_agent_id")
    private String currentAgentId;

    @Column(name = "conversation_thread_id")
    private String conversationThreadId;

    @Version
    @Column(name = "version")
    private int version;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public void setStatus(SessionStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCurrentAgentId() {
        return currentAgentId;
    }

    public void setCurrentAgentId(String currentAgentId) {
        this.currentAgentId = currentAgentId;
    }

    public String getConversationThreadId() {
        return conversationThreadId;
    }

    public void setConversationThreadId(String conversationThreadId) {
        this.conversationThreadId = conversationThreadId;
    }
    public int getVersion() {
        return version;
    }
}
