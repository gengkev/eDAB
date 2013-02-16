package com.desklampstudios.edab;

import java.util.Date;
import java.util.List;

import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.EntitySubclass;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Parent;

@EntitySubclass(index=true)
public class PersonalNote extends Entry {
	@Parent User parent;
	@Index Date date;
	List<Ref<User>> sharedWith;
}
