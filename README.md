mybatis拼接SQL的语法较为复杂。为此我实现了一个SQL解析器，能根据变量的上下文，推断如何拼接SQL，**避免**使用`if`,`trim`,`where`, `set`,`foreach`等标签。以下介绍基本的用法。
 #  准备
用
```xml
<dependency>
	<groupId>com.github.huangzhouhong</groupId>
	<artifactId>mybatis</artifactId>
	<version>0.0.2</version>
</dependency>
```
替换
```xml
<dependency>
    <groupId>org.mybatis</groupId>
    <artifactId>mybatis</artifactId>
    <version>3.5.3</version>
</dependency>
```
# 1.
```java
	@Sql("select * from blog where state='ACTIVE' and title=#?{title}")
	List<Blog> findActiveBlogWithTitleLike(String title);
```
`#?{}`表示参数是可选的，`#{}`则是必选的。可选参数为`null`时会根据上下文自动删除。为了保证原有功能不受影响，新增了`@Sql`注解。
上述相当于：
```XML
<select id="findActiveBlogWithTitleLike"
     resultType="Blog">
  SELECT * FROM BLOG
  WHERE state = ‘ACTIVE’
  <if test="title != null">
    AND title like #{title}
  </if>
</select>
```
# 2.
接口声明
```java
List<Blog> findBlogLike(String state, String title, Author author);
```
xml
```xml
	<sql id="findBlogLike">
		SELECT * FROM BLOG
		WHERE state = #?{state} AND title like #?{title}
		AND author_name like #?{author.userName}
	</sql>
```
相当于:
```xml
<select id="findActiveBlogLike"
     resultType="Blog">
  SELECT * FROM BLOG
  <where>
    <if test="state != null">
         state = #{state}
    </if>
    <if test="title != null">
        AND title like #{title}
    </if>
    <if test="author != null and author.name != null">
        AND author_name like #{author.name}
    </if>
  </where>
</select>
```
当某一参数为空，框架能自动删除字符串且处理好`and`、`or`和逗号等等，所有参数为空(且都为可选)时能自动删除`where`。总而言之，解析器从上下文推测该如何处理字符串。

# 3.
```xml
	<sql id="updateAuthorIfNecessary">
		update Author set
			user_name=#?{username},
			password=#?{password},
			email=#?{email},
			mobile=#?{mobile}
		where author_id=#{authorId}
	</sql>
```
同样，不用担心逗号处理的问题，相当于
```xml
<update id="updateAuthorIfNecessary">
  update Author
    <set>
      <if test="username != null">username=#{username},</if>
      <if test="password != null">password=#{password},</if>
      <if test="email != null">email=#{email},</if>
      <if test="mobile!= null">mobile=#{mobile}</if>
    </set>
  where author_id=#{author_id}
</update>
```
# 4. 
接口
```java
	List<Post> selectPostIn(List<Integer> ids);
```
```xml
	<sql id="selectPostIn">
		SELECT *
		FROM POST P
		WHERE post_id in (#{ids})
	</sql>
```
相当于
```xml
<select id="selectPostIn" resultType="domain.blog.Post">
  SELECT *
  FROM POST P
  WHERE post_id in
  <foreach item="item" index="index" collection="list"
      open="(" separator="," close=")">
        #{item}
  </foreach>
</select>
```
1. 解析器会自动判断传到`IN`子句中的变量是不是集合，如果是集合，则会把它展开成多个变量。
2. 假设方法签名改成`selectPostIn(List<BlogPost> blogPosts);`，且要查找所有`BlogPost.post_id`，可以写成
```xml
	<sql id="selectPostIn">
		SELECT *
		FROM POST P
		WHERE post_id in (#{post_id})
	</sql>
```
也可以写成`#{blogPosts.post_id}`或者`#{list.post_id}`。相当于
```xml
<select id="selectPostIn" resultType="domain.blog.Post">
  SELECT *
  FROM POST P
  WHERE post_id in
  <foreach item="item" index="index" collection="list"
      open="(" separator="," close=")">
        #{item.post_id}
  </foreach>
</select>
```
# 5. insert
接口
```java
	@Sql("insert into blog (blog_id,title) values (#{blogId},#{title})")
	int insert(Blog... blogs);
```
可以插入一个实体也可以批量插入，用例：
```java
blogMapper.insert(blog1);
blogMapper.insert(blog1,blog2);
blogMapper.insert(blogs);
```
这里接口只有一个参数，所以`#{blogId}`和`#{title}`没有歧义。如果有多个参数，可以使用`#{blogs.blogId}`

# 6. 替换掉script标签
以往，要在带注解的映射器接口类中使用动态 SQL，可以使用 script 元素。比如:
```java
    @Update({"<script>",
      "update Author",
      "  <set>",
      "    <if test='username != null'>username=#{username},</if>",
      "    <if test='password != null'>password=#{password},</if>",
      "    <if test='email != null'>email=#{email},</if>",
      "    <if test='bio != null'>bio=#{bio}</if>",
      "  </set>",
      "where id=#{id}",
      "</script>"})
    void updateAuthorValues(Author author);
```
可以写成：
```java
    @Sql({"update Author",
      "set username=#{username},",
      "password=#{password},",
      "email=#{email},",
      "bio=#{bio}",
      "where id=#{id}"})
    void updateAuthorValues(Author author);
```

# 总结
1. 设计目标是框架能理解SQL，并根据上下文能处理变量。
2. 原有注解不受影响，功能也不变。如果要使用上述功能，只需使用`@Sql`或`<sql>`，现有项目迁移过来不需要做任何修改。
3. 本文源码：[https://github.com/huangzhouhong/mybatis-demo](https://github.com/huangzhouhong/mybatis-demo)
4. 项目源码：[https://github.com/huangzhouhong/mybatis-3](https://github.com/huangzhouhong/mybatis-3)
![讨论群](https://upload-images.jianshu.io/upload_images/6719795-c9f4d4f2ffbeed53.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

