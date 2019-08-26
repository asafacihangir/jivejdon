package com.jdon.jivejdon.event.domain.consumer.write;

import com.jdon.annotation.Consumer;
import com.jdon.async.disruptor.EventDisruptor;
import com.jdon.domain.message.DomainEventHandler;
import com.jdon.jivejdon.event.bus.cqrs.query.EventBusHandler;
import com.jdon.jivejdon.event.bus.cqrs.query.UpdateMessageEventHandler;
import com.jdon.jivejdon.model.ForumThread;
import com.jdon.jivejdon.model.event.ThreadNameSavedEvent;
import com.jdon.jivejdon.repository.MessagePageIteratorSolver;
import com.jdon.jivejdon.repository.builder.ForumAbstractFactory;
import com.jdon.jivejdon.repository.builder.MessageRepositoryDao;
import com.jdon.jivejdon.repository.builder.ThreadRepositoryDao;

import java.util.Optional;

@Consumer("saveName")
public class ThreadNameListener implements DomainEventHandler {

	private final ThreadRepositoryDao threadRepositoryDao;

	private final MessageRepositoryDao messageRepositoryDao;

	private final ForumAbstractFactory forumAbstractFactory;

	private final EventBusHandler eventBusHandler;

	public ThreadNameListener(ThreadRepositoryDao threadRepositoryDao, MessageRepositoryDao
			messageRepositoryDao, ForumAbstractFactory forumAbstractFactory, MessagePageIteratorSolver messagePageIteratorSolver) {
		super();
		this.threadRepositoryDao = threadRepositoryDao;
		this.messageRepositoryDao = messageRepositoryDao;
		this.forumAbstractFactory = forumAbstractFactory;
		this.eventBusHandler = new UpdateMessageEventHandler( forumAbstractFactory, messagePageIteratorSolver);
	}

	public void onEvent(EventDisruptor event, boolean endOfBatch) throws Exception {

		ThreadNameSavedEvent es = (ThreadNameSavedEvent) event.getDomainMessage().getEventSource();
		Long threadId = es.getThreadId();
		Optional<ForumThread> forumThreadOptional = forumAbstractFactory.getThread(threadId);
		try {
			threadRepositoryDao.updateThreadName(es.getName(), forumThreadOptional.get());
			eventBusHandler.refresh(forumThreadOptional.get().getRootMessage().getMessageId());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}