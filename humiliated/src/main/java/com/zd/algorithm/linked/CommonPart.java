package com.zd.algorithm.linked;

import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

@Data
public class CommonPart {

    private LinkedNode linkedNode1;

    private LinkedNode linkedNode2;

    /**
     * 返回有序链表里相同的元素
     */
    public List<Integer> commonPart() {
        List<Integer> list = Lists.newArrayList();
        LinkedNode head1 = this.linkedNode1;
        LinkedNode head2 = this.linkedNode2;
        while (head1 != null && head2 != null) {
            if (head1.getValue() < head2.getValue()) {
                head1 = head1.getNext();
            } else if (head1.getValue() > head2.getValue()) {
                head2 = head2.getNext();
            } else {
                //相等
                list.add(head1.getValue());
                head1 = head1.getNext();
                head2 = head2.getNext();
            }
        }
        return list;
    }

    /**
     * 删除倒数N个节点
     */
    public LinkedNode removeLastNode(int index) {
        if (this.linkedNode1 == null || index < 1) {
            return linkedNode1;
        }
        LinkedNode head = linkedNode1;
        while (head != null) {
            index--;
            head = head.getNext();
        }
        if (index == 0) {
            head = head.getNext();
        }
        if (index < 0) {
            head = linkedNode1;
            while (++index != 0) {
                head = head.getNext();
            }
            head.setNext(head.getNext().getNext());
        }
        return head;
    }

    public static void main(String[] args) {
        CommonPart commonPart = new CommonPart();
        commonPart.setLinkedNode1(new LinkedNode(2).next(5).next(7));
        commonPart.setLinkedNode2(new LinkedNode(2).next(5).next(7));
        System.out.println(commonPart.commonPart());
        commonPart.removeLastNode(1);
        System.out.println(commonPart.getLinkedNode1());
    }
}
