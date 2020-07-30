package com.github.llyb120.namilite;

public class ByteCodeLoader extends HotLoader {

    private String clzName;
    private byte[] bytes;

    public ByteCodeLoader(String clzName, byte[] bs){
        this.clzName = clzName;
        this.bytes = bs;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        Class clz = findLoadedClass(name);
        if (clz != null) {
            return clz;
        }
        if(name.equals(clzName)){
            return defineClass(clzName, bytes, 0, bytes.length);
        }
        return super.findClass(name);
    }
}
