package com.desklampstudios.edab;

import java.util.Date;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.EntitySubclass;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;

@EntitySubclass(index=true)
public class Assignment extends Entry {
	
	private Assignment() {
		super();
	}
	public Assignment(Long id, Key<Course> parent) {
		super(id);
		this.parent = parent;
	}
	
	@Parent Key<Course> parent;
	@Index public Date dueDate;
	String bb_id;
}
