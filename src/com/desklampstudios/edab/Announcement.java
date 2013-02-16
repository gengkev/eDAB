package com.desklampstudios.edab;

import com.googlecode.objectify.annotation.EntitySubclass;
import com.googlecode.objectify.annotation.Parent;

@EntitySubclass(index=true)
public class Announcement extends Entry {
	@Parent Course parent;
	String bb_id;
}
