-- MySQL dump 10.13  Distrib 8.0.41, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: online_study_exam
-- ------------------------------------------------------
-- Server version	8.0.41

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `course`
--
create database if not exists `online_study_exam` default character set utf8mb4 collate utf8mb4_general_ci;
use `online_study_exam`;
DROP TABLE IF EXISTS `course`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '课程标题',
  `cover` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '课程封面',
  `description` text COLLATE utf8mb4_general_ci COMMENT '课程简介',
  `category_id` int DEFAULT NULL COMMENT '分类ID',
  `teacher_id` int NOT NULL COMMENT '教师ID',
  `course_code` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '课程码(用于加课)',
  `is_public` tinyint(1) DEFAULT '1' COMMENT '是否公开: 0私有, 1公开',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `course_code` (`course_code`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='课程表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `course`
--

LOCK TABLES `course` WRITE;
/*!40000 ALTER TABLE `course` DISABLE KEYS */;
INSERT INTO `course` VALUES (1,'Java编程入门','/uploads/c6071b7c-790a-4eac-901f-2730fc3b279a.png','从零开始学习Java编程语言',1,2,'JAVA101',1,'2026-04-25 11:31:03','2026-04-25 11:31:03'),(2,'Vue3实战开发','/uploads/80810978-cccc-4fe8-a6d7-c8069539e872.png','掌握最新的前端开发框架',1,2,'VUE202',1,'2026-04-25 11:31:03','2026-04-25 12:06:42'),(3,'职场口语','/uploads/760d3f80-cc4b-4558-9135-843e8cde7ace.webp','提升您的职业沟通能力',3,2,'TALK303',0,'2026-04-25 11:31:03','2026-04-25 12:06:42');
/*!40000 ALTER TABLE `course` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `course_chapter`
--

DROP TABLE IF EXISTS `course_chapter`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course_chapter` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `course_id` int NOT NULL COMMENT '课程ID',
  `title` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '章节标题',
  `sort` int DEFAULT '0' COMMENT '排序',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='课程章节表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `course_chapter`
--

LOCK TABLES `course_chapter` WRITE;
/*!40000 ALTER TABLE `course_chapter` DISABLE KEYS */;
INSERT INTO `course_chapter` VALUES (1,1,'Java环境搭建',1,'2026-04-25 11:31:03'),(2,1,'基础语法',2,'2026-04-25 11:31:03'),(3,1,'面向对象',3,'2026-04-25 11:31:03'),(4,2,'基础指令',1,'2026-04-25 11:31:03'),(5,2,'组件开发',2,'2026-04-25 11:31:03');
/*!40000 ALTER TABLE `course_chapter` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `course_resource`
--

DROP TABLE IF EXISTS `course_resource`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course_resource` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `chapter_id` int NOT NULL COMMENT '章节ID',
  `title` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '资源名称',
  `type` varchar(20) COLLATE utf8mb4_general_ci NOT NULL COMMENT '资源类型: VIDEO, PDF, PPT, TEXT',
  `url` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '资源链接/路径',
  `content` longtext COLLATE utf8mb4_general_ci COMMENT '文本内容(富文本类型使用)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='课程资源表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `course_resource`
--

LOCK TABLES `course_resource` WRITE;
/*!40000 ALTER TABLE `course_resource` DISABLE KEYS */;
INSERT INTO `course_resource` VALUES (1,1,'JDK安装教程','VIDEO','/uploads/jdk_install.mp4','/uploads/90d58910-2dd5-4329-b9ad-89278b79adb2.mp4','2026-04-25 11:31:03'),(2,1,'环境变量配置指南','DOC','/uploads/env_config.pdf','/uploads/f6e6ac26-ae61-4d49-927c-e811cc7d7be1.pdf','2026-04-25 11:31:03'),(3,2,'变量与数据类型','DOC','本节介绍Java的基本数据类型...','/uploads/c28f1cdf-9b3e-4ea4-92d7-c5060bbd96e9.pdf','2026-04-25 11:31:03'),(4,1,'什么是Java','VIDEO',NULL,'/uploads/aef0c54d-335c-4e13-bdab-6334e22c7640.mp4','2026-04-25 11:52:26'),(5,2,'java基础','VIDEO',NULL,'/uploads/aa34685f-e87c-4a70-8620-dc486529ec89.mp4','2026-04-26 16:35:16'),(6,3,'面向对象基础','DOC',NULL,'/uploads/bd9e0482-bdbf-4dec-95b4-a5be2ca1e4f0.pdf','2026-04-26 16:35:40');
/*!40000 ALTER TABLE `course_resource` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `course_selection`
--

DROP TABLE IF EXISTS `course_selection`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course_selection` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `student_id` int NOT NULL COMMENT '学生ID',
  `course_id` int NOT NULL COMMENT '课程ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '选课时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='选课关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `course_selection`
--

LOCK TABLES `course_selection` WRITE;
/*!40000 ALTER TABLE `course_selection` DISABLE KEYS */;
INSERT INTO `course_selection` VALUES (1,4,1,'2026-04-25 11:44:29'),(2,5,1,'2026-04-26 16:36:24');
/*!40000 ALTER TABLE `course_selection` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `exam`
--

DROP TABLE IF EXISTS `exam`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `exam` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '考试名称',
  `course_id` int DEFAULT NULL COMMENT '绑定课程ID',
  `duration` int NOT NULL DEFAULT '60' COMMENT '考试时长(分钟)',
  `total_points` int DEFAULT '0' COMMENT '总分',
  `pass_score` int DEFAULT '60' COMMENT '及格分为',
  `max_attempts` int DEFAULT '1' COMMENT '可考试次数',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='考试/试卷表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `exam`
--

LOCK TABLES `exam` WRITE;
/*!40000 ALTER TABLE `exam` DISABLE KEYS */;
INSERT INTO `exam` VALUES (1,'Java期中考试',1,60,25,15,1,'2026-04-01 00:00:00','2026-12-31 23:59:59','2026-04-25 11:31:03'),(2,'java基础类',1,60,25,15,10,'2026-04-25 00:00:00','2026-04-28 00:00:00','2026-04-25 11:39:06'),(3,'java入门考试',1,60,10,5,1,'2026-04-25 00:00:00','2026-04-30 00:00:00','2026-04-25 12:20:50'),(4,'Java',1,60,30,15,2,'2026-04-26 00:00:00','2026-05-31 00:00:00','2026-04-26 16:39:03');
/*!40000 ALTER TABLE `exam` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `exam_question`
--

DROP TABLE IF EXISTS `exam_question`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `exam_question` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `exam_id` int NOT NULL COMMENT '考试ID',
  `question_id` int NOT NULL COMMENT '题目ID',
  `score` int NOT NULL COMMENT '该题在本次考试的分值',
  `sort` int DEFAULT '0' COMMENT '题目排序',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='试卷题目关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `exam_question`
--

LOCK TABLES `exam_question` WRITE;
/*!40000 ALTER TABLE `exam_question` DISABLE KEYS */;
INSERT INTO `exam_question` VALUES (15,2,1,5,1),(16,2,2,5,2),(17,2,3,5,3),(18,2,4,10,4),(19,1,1,5,1),(20,1,2,5,2),(21,1,3,5,3),(22,1,4,10,4),(23,3,2,5,1),(24,3,3,5,2),(25,4,1,5,1),(26,4,2,5,2),(27,4,5,5,3),(28,4,3,5,4),(29,4,4,10,5);
/*!40000 ALTER TABLE `exam_question` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `exam_record`
--

DROP TABLE IF EXISTS `exam_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `exam_record` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `exam_id` int NOT NULL COMMENT '考试ID',
  `student_id` int NOT NULL COMMENT '学生ID',
  `score` int DEFAULT '0' COMMENT '最终得分',
  `start_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '考试开始时间',
  `submit_time` datetime DEFAULT NULL COMMENT '交卷时间',
  `status` tinyint DEFAULT '0' COMMENT '状态: 0进行中, 1已提交(待批改), 2已判分',
  `attempt` int DEFAULT '1' COMMENT '第几次尝试',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='考试记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `exam_record`
--

LOCK TABLES `exam_record` WRITE;
/*!40000 ALTER TABLE `exam_record` DISABLE KEYS */;
INSERT INTO `exam_record` VALUES (1,2,4,0,'2026-04-26 11:14:16',NULL,0,1),(2,1,4,10,'2026-04-26 11:52:46','2026-04-26 11:53:02',2,1),(3,2,4,0,'2026-04-26 12:12:14',NULL,0,2),(4,2,4,25,'2026-04-26 12:17:42','2026-04-26 12:17:55',2,3),(5,3,4,0,'2026-04-26 12:18:22',NULL,0,1),(6,2,4,25,'2026-04-26 12:19:09','2026-04-26 12:19:27',2,4),(7,4,5,30,'2026-04-26 16:39:15','2026-04-26 16:39:43',2,1),(8,3,5,0,'2026-04-26 16:40:04',NULL,0,1),(9,4,5,30,'2026-04-26 16:43:05','2026-04-26 16:43:23',2,2);
/*!40000 ALTER TABLE `exam_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `exam_record_detail`
--

DROP TABLE IF EXISTS `exam_record_detail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `exam_record_detail` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `record_id` int NOT NULL COMMENT '考试记录ID',
  `question_id` int NOT NULL COMMENT '题目ID',
  `user_answer` text COLLATE utf8mb4_general_ci COMMENT '用户答案',
  `is_correct` tinyint(1) DEFAULT NULL COMMENT '是否正确: 0错, 1对',
  `score` int DEFAULT '0' COMMENT '得分',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='考试答题详情表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `exam_record_detail`
--

LOCK TABLES `exam_record_detail` WRITE;
/*!40000 ALTER TABLE `exam_record_detail` DISABLE KEYS */;
INSERT INTO `exam_record_detail` VALUES (1,2,1,'1',0,0),(2,2,2,'2',0,0),(3,2,3,'T',0,0),(4,2,4,'main',1,10),(5,4,1,'final',0,5),(6,4,2,'String',0,5),(7,4,3,'T',0,5),(8,4,4,'main',0,10),(9,6,1,'final',1,5),(10,6,2,'String',1,5),(11,6,3,'错误',1,5),(12,6,4,'main',1,10),(13,7,1,'final',0,5),(14,7,2,'String',0,5),(15,7,3,'正确',0,5),(16,7,4,'main',0,10),(17,7,5,'String,boolean,final,int',0,5),(18,9,1,'final',1,5),(19,9,2,'String',1,5),(20,9,3,'错误',1,5),(21,9,4,'main',1,10),(22,9,5,'String,boolean,final,int',1,5);
/*!40000 ALTER TABLE `exam_record_detail` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `learning_progress`
--

DROP TABLE IF EXISTS `learning_progress`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `learning_progress` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `student_id` int NOT NULL COMMENT '学生ID',
  `resource_id` int NOT NULL COMMENT '资源ID',
  `progress` int DEFAULT '0' COMMENT '学习百分比(0-100)',
  `is_finished` tinyint(1) DEFAULT '0' COMMENT '是否学完',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='学习进度表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `learning_progress`
--

LOCK TABLES `learning_progress` WRITE;
/*!40000 ALTER TABLE `learning_progress` DISABLE KEYS */;
INSERT INTO `learning_progress` VALUES (1,4,1,100,1,'2026-04-26 11:08:42'),(2,4,2,100,1,'2026-04-26 11:10:23'),(3,5,1,10,0,'2026-04-26 16:36:34'),(4,5,2,100,1,'2026-04-26 16:36:39'),(5,5,3,100,1,'2026-04-26 16:36:59'),(6,5,6,100,1,'2026-04-26 16:37:06');
/*!40000 ALTER TABLE `learning_progress` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `question_bank`
--

DROP TABLE IF EXISTS `question_bank`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `question_bank` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `type` tinyint NOT NULL COMMENT '题型: 1单选题, 2多选题, 3判断题, 4填空题',
  `content` text COLLATE utf8mb4_general_ci NOT NULL COMMENT '题目内容',
  `options` text COLLATE utf8mb4_general_ci COMMENT '选项(JSON数组)',
  `answer` text COLLATE utf8mb4_general_ci NOT NULL COMMENT '标准答案',
  `analysis` text COLLATE utf8mb4_general_ci COMMENT '题目解析',
  `score` int DEFAULT '5' COMMENT '默认分值',
  `difficulty` tinyint DEFAULT '2' COMMENT '难度: 1简单, 2中等, 3困难',
  `category_id` int DEFAULT NULL COMMENT '知识点/分类ID',
  `course_id` int DEFAULT NULL COMMENT '关联课程ID',
  `knowledge_point` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '知识点标签',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='题库表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `question_bank`
--

LOCK TABLES `question_bank` WRITE;
/*!40000 ALTER TABLE `question_bank` DISABLE KEYS */;
INSERT INTO `question_bank` VALUES (1,1,'Java中定义常量的关键字是？','[\"var\",\"final\",\"const\",\"static\"]','final',NULL,5,1,4,1,'修饰符','2026-04-25 11:31:03','2026-04-25 12:17:17'),(2,1,'以下哪个不是Java的基本数据类型？','[\"int\",\"double\",\"String\",\"boolean\"]','String',NULL,5,1,4,1,'数据类型','2026-04-25 11:31:03','2026-04-25 12:17:23'),(3,3,'Java语言支持多重继承。',NULL,'错误',NULL,5,2,4,1,'类继承','2026-04-25 11:31:03','2026-04-25 12:17:34'),(4,4,'Java程序的入口方法是_____方法。',NULL,'main',NULL,10,1,4,1,'基础','2026-04-25 11:31:03','2026-04-25 12:19:31'),(5,2,'Java里面的关键字是什么？','{\"A\":\"String\",\"B\":\"int\",\"C\":\"boolean\",\"D\":\"final\"}','String,int,boolean,final','',5,1,NULL,1,NULL,'2026-04-25 12:18:24','2026-04-25 12:19:18'),(6,1,'Java','{\"A\":\"1\",\"B\":\"2\",\"C\":\"3\",\"D\":\"4\"}','1','',5,1,NULL,1,NULL,'2026-04-26 16:37:55','2026-04-26 16:37:55');
/*!40000 ALTER TABLE `question_bank` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_category`
--

DROP TABLE IF EXISTS `sys_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_category` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '分类名称',
  `parent_id` int DEFAULT '0' COMMENT '父分类ID',
  `type` tinyint DEFAULT '1' COMMENT '类型: 1课程分类, 2题库分类',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='分类表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_category`
--

LOCK TABLES `sys_category` WRITE;
/*!40000 ALTER TABLE `sys_category` DISABLE KEYS */;
INSERT INTO `sys_category` VALUES (1,'计算机科学',0,1,'2026-04-25 11:31:03'),(2,'艺术设计',0,1,'2026-04-25 11:31:03'),(3,'语言学习',0,1,'2026-04-25 11:31:03'),(4,'Java基础',0,2,'2026-04-25 11:31:03'),(5,'数据库原理',0,2,'2026-04-25 11:31:03'),(6,'前端技术',0,2,'2026-04-25 11:31:03'),(7,'软件工程',0,1,'2026-04-25 11:31:30'),(8,'移动端开发',0,2,'2026-04-25 11:31:38'),(9,'python开发',0,2,'2026-04-25 11:31:52'),(10,'计算机网络',0,1,'2026-04-26 16:24:09'),(11,'web开发',0,2,'2026-04-26 16:24:21');
/*!40000 ALTER TABLE `sys_category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_config`
--

DROP TABLE IF EXISTS `sys_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_config` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_key` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '配置键',
  `config_value` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '配置值',
  `remark` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `config_key` (`config_key`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_config`
--

LOCK TABLES `sys_config` WRITE;
/*!40000 ALTER TABLE `sys_config` DISABLE KEYS */;
INSERT INTO `sys_config` VALUES (1,'sys_name','智学在线考试系统','系统名称'),(2,'allow_register','true','是否允许学生注册');
/*!40000 ALTER TABLE `sys_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_notice`
--

DROP TABLE IF EXISTS `sys_notice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_notice` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '公告标题',
  `content` text COLLATE utf8mb4_general_ci NOT NULL COMMENT '公告内容',
  `target_role` varchar(20) COLLATE utf8mb4_general_ci DEFAULT 'ALL' COMMENT '发布对象: ALL, TEACHER, STUDENT',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='公告表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_notice`
--

LOCK TABLES `sys_notice` WRITE;
/*!40000 ALTER TABLE `sys_notice` DISABLE KEYS */;
INSERT INTO `sys_notice` VALUES (1,'欢迎使用智学系统','祝各位同学学习愉快！','ALL','2026-04-25 11:31:04'),(2,'关于手动批改的通知','请各位教师及时批改填空题。','TEACHER','2026-04-25 11:31:04'),(3,'好好学习','好好学习','STUDENT','2026-04-25 11:33:10'),(4,'test公告','test公告','ALL','2026-04-26 16:24:39');
/*!40000 ALTER TABLE `sys_notice` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_user`
--

DROP TABLE IF EXISTS `sys_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '密码',
  `nickname` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '头像',
  `email` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '性别',
  `role` varchar(20) COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'STUDENT' COMMENT '角色: ADMIN, TEACHER, STUDENT',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态: 0禁用, 1启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user`
--

LOCK TABLES `sys_user` WRITE;
/*!40000 ALTER TABLE `sys_user` DISABLE KEYS */;
INSERT INTO `sys_user` VALUES (1,'admin','$2a$10$/a88sQPYeMuU64birkWloOW9igoiUAVr9H6Mr4lXIg/Kk4pka0u96','超级管理员','/files/b149645f-edb4-4b6d-a6a1-4e9cb8e4af05.jpg',NULL,NULL,'ADMIN',1,'2026-04-25 11:12:53','2026-04-25 11:21:17'),(2,'teacher','$2a$10$/a88sQPYeMuU64birkWloOW9igoiUAVr9H6Mr4lXIg/Kk4pka0u96','演示教师',NULL,NULL,NULL,'TEACHER',1,'2026-04-25 11:12:53','2026-04-25 11:21:17'),(3,'student','$2a$10$GDr/CgpuRs9ehdsRoL1jl.xbj7jMxHtm/v.QkQJq8b/SiKqs0IHpW','演示学生',NULL,NULL,NULL,'STUDENT',0,'2026-04-25 11:12:53','2026-04-26 16:23:43'),(4,'user1','$2a$10$/a88sQPYeMuU64birkWloOW9igoiUAVr9H6Mr4lXIg/Kk4pka0u96','用户1',NULL,'1@qq.com','18111111111111','STUDENT',1,'2026-04-25 11:21:08','2026-04-25 11:21:08'),(5,'user2','$2a$10$R8mkY.Ywg9Y4Ueo7RDgiqe3oMDlzp9G7TbNu5Lg..woHPmjFnP/zS','用户2','/uploads/56e16730-9255-4143-8d3a-4ee0dbfe8d86.png','123@qq.com','1811111111111','STUDENT',1,'2026-04-26 16:25:00','2026-04-26 16:25:00'),(6,'teacher2','$2a$10$QCH1ksMa5Th1EIl.C.H4dOY2dptIDgB7NmVf7hqVgNSs0Iq0X.Tcm','teacher2',NULL,NULL,NULL,'TEACHER',1,'2026-04-26 16:27:30','2026-04-26 16:27:30');
/*!40000 ALTER TABLE `sys_user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-04-28 17:03:56
