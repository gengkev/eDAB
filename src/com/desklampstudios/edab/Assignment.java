package com.desklampstudios.edab;

import java.util.Date;

import com.googlecode.objectify.annotation.EntitySubclass;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;

@EntitySubclass(index=true)
public class Assignment extends Entry {
	@Parent Course parent;
	@Index Date dueDate;
	String bb_id;
}
