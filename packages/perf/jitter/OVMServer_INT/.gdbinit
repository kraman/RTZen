# GDB macros for debugging Ovm virtual machines.
directory /users/kraman/RTZEN/tools/OpenVM/OpenVM_src/src/native/interpreter

define pclass
print (char *) ((struct HEADER *) ($arg0))->_blueprint_->dbg_string->values
end
document pclass
print an object's blueprint string
end

