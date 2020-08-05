
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * @Description:
 * @Author:boxiao
 * @Data: 2020/5/25 22:23
 */

@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisTest {
//    @Autowired
//    RedisService redisService;

//    @Autowired
//    MQSender mqSender;
//    @Autowired
//    private KafkaTemplate<String,String> kafkaTemplate;

//    @Resource
//    KafkaConsumer kafkaConsumer;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    //    @Test
//    public void mqTest(){
//        kafkaTemplate.send("test", "hello!!!");
//    }
//
    @Test
    public void rTest() {
        redisTemplate.opsForValue().set("1", "1");
    }

    private Logger logger = LoggerFactory.getLogger(RedisTest.class);

//    @Autowired
//    private DataSource dataSource;

//    @Test
//    public void run() {
//        logger.info("[run][获得数据源：{}]", dataSource.getClass());
//    }

}

