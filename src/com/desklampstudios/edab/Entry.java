package com.desklampstudios.edab;

import java.util.Date;
import java.util.List;

import com.google.appengine.api.datastore.Text;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Embed;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

@Entity
public class Entry {
	@Id Long id;
	@Index String name;
	Text content;
	Date updated;
	@Index Date created;
	List<Revision> revisions;
	List<Comment> comments;
	
	@Embed
	static class Revision {
		Text content;
		@Index Date created;
		Key<User> user;
	}
	@Embed
	static class Comment {
		Text content;
		@Index Date created;
		Key<User> user;
	}
}
