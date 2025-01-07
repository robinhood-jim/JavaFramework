package com.robin.core.fileaccess.util;

import stormpot.Allocator;
import stormpot.Slot;

public class CalculatorAllocator implements Allocator<Calculator> {
    public CalculatorAllocator(){

    }

    @Override
    public Calculator allocate(Slot slot) throws Exception {
        return new Calculator(slot);
    }

    @Override
    public void deallocate(Calculator calculator) throws Exception {
        calculator.close();
    }
}
