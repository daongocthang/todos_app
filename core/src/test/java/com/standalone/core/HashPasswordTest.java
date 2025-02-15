package com.standalone.core;

import static org.junit.Assert.assertTrue;

import com.standalone.core.adapter.utils.EncUtil;

import org.junit.Test;

public class HashPasswordTest {
    @Test
    public void testHashPassword(){
        String pw="12345a@";
        String hashedPassword= EncUtil.hash(pw);
        System.out.println(hashedPassword);
        assertTrue(EncUtil.verify(pw,hashedPassword));
    }
}
