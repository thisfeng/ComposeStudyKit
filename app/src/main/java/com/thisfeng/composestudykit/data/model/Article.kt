package com.thisfeng.composestudykit.data.model

import com.squareup.moshi.JsonClass

/**
 * 文章数据模型
 */
@JsonClass(generateAdapter = true)
data class Article(
    val id: Int,
    val title: String,
    val link: String,
    val author: String,
    val shareUser: String,
    val desc: String,
    val publishTime: Long,
    val chapterName: String,
    val superChapterName: String,
    val collect: Boolean = false,
    val fresh: Boolean = false,
    val type: Int = 0,
    val userId: Int = 0,
    val visible: Int = 1,
    val zan: Int = 0,
    val tags: List<Tag> = emptyList()
)

/**
 * 标签数据模型
 */
@JsonClass(generateAdapter = true)
data class Tag(
    val name: String,
    val url: String
)

/**
 * 文章列表数据模型
 */
@JsonClass(generateAdapter = true)
data class ArticleList(
    val curPage: Int,
    val offset: Int,
    val over: Boolean,
    val pageCount: Int,
    val size: Int,
    val total: Int,
    val datas: List<Article>
)

/**
 * Banner 数据模型
 */
@JsonClass(generateAdapter = true)
data class Banner(
    val id: Int,
    val desc: String,
    val imagePath: String,
    val isVisible: Int,
    val order: Int,
    val title: String,
    val type: Int,
    val url: String
)