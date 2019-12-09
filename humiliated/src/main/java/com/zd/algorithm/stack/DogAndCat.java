package com.zd.algorithm.stack;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.LinkedList;
import java.util.Queue;

/**
 * 猫狗队列
 */
@Data
public class DogAndCat {

    private Queue<PetQueue<Cat>> catQueue;

    private Queue<PetQueue<Dog>> dogQueue;

    public DogAndCat() {
        //双向链表
        catQueue = new LinkedList<>();
        dogQueue = new LinkedList<>();
    }


    /**
     * 动物
     */
    @Data
    @AllArgsConstructor
    public class Pet {

        private Pets type;

    }

    enum Pets {
        DOG, CAT
    }

    public class Dog extends Pet {
        public Dog() {
            super(Pets.DOG);
        }
    }

    public class Cat extends Pet {
        public Cat() {
            super(Pets.CAT);
        }
    }

    @Data
    public class PetQueue<T extends Pet> {

        private T pet;

        private long time;

        public PetQueue(T pet) {
            this.pet = pet;
            this.time = System.currentTimeMillis();
        }
    }

    public void add(Pet pet) {
        switch (pet.getType()) {
            case DOG:
                this.dogQueue.add(new PetQueue<>((Dog) pet));
                break;
            case CAT:
                this.catQueue.add(new PetQueue<>((Cat) pet));
                break;
        }
    }

    public Dog pollDog() {
        if (this.dogQueue.isEmpty()) {
            throw new RuntimeException(" dogQueue isEmpty  ");
        }
        return this.dogQueue.poll().getPet();
    }

    public Cat pollCat() {
        if (this.catQueue.isEmpty()) {
            throw new RuntimeException(" dogQueue isEmpty  ");
        }
        return this.catQueue.poll().getPet();
    }

    public Pet pollAll() {
        if (isEmpty()) {
            throw new RuntimeException(" queue isEmpty  ");
        } else if (isCatEmpty()) {
            return this.dogQueue.poll().getPet();
        } else if (isDogEmpty()) {
            return this.catQueue.poll().getPet();
        } else if (this.catQueue.peek().getTime() > this.dogQueue.peek().getTime()) {
            return this.catQueue.poll().getPet();
        }
        return this.dogQueue.poll().getPet();
    }

    public boolean isEmpty() {
        return this.catQueue.isEmpty() && this.dogQueue.isEmpty();
    }

    public boolean isDogEmpty() {
        return this.dogQueue.isEmpty();
    }

    public boolean isCatEmpty() {
        return this.catQueue.isEmpty();
    }
}
