# Priority Queueing Examples

This repository contains some examples on how priority queueing
can be achieved with RabbitMQ (which as of early 2014 does not
support priorities).


## Hi-Lo Priority Consumer

Many applications have a high volume rate of low priority tasks and
a low rate of high priority task, with the requirement that high
priority tasks are completed eventually without waiting for
the entire existing backlog of low priority tasks.

To achieve this with RabbitMQ, use multiple queues (this example
uses two) with separate channels (or connections, if you want
to use separate thread pools for deliveries), and two queues, e.g.

 * `priorities.high`
 * `priorities.low`

Then add a consumer or multiple consumers to each queue.

[HiLoPriorityConsumers](./hilo-consumer/src/main/java/com/novemberain/consumers/HiLoPriorityConsumers.java) demonstrates this approach.


## License & Copyright

(c) Michael S. Klishin, 2014

Released under the [MIT license](http://opensource.org/licenses/MIT).
