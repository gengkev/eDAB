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
	
	protected Entry() {
		this.updated = this.created = new Date();
	}
	
	public Entry(Long id) {
		this();
		this.id = id;
	}
	
	@Id public Long id;
	@Index public String name;
	public Text content;
	@Index public Date updated;
	public Date created;
	public List<Revision> revisions;
	public List<Comment> comments;
	
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
