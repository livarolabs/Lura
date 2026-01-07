package com.lura.data.engine

import com.lura.domain.engine.BookContent
import com.lura.domain.engine.Chapter
import com.lura.domain.engine.EpubParser
import javax.inject.Inject

class StubEpubParser @Inject constructor() : EpubParser {
    override suspend fun parseBook(filePath: String): BookContent {
        return BookContent(
            title = "Moby Dick",
            author = "Herman Melville",
            chapters = listOf(
                Chapter(
                    title = "Chapter 1. Loomings.",
                    elements = listOf(
                        com.lura.domain.engine.ReaderElement.Text(
                            content = "Call me Ishmael. Some years ago—never mind how long precisely—having little or no money in my purse, and nothing particular to interest me on shore, I thought I would sail about a little and see the watery part of the world. It is a way I have of driving off the spleen and regulating the circulation. Whenever I find myself growing grim about the mouth; whenever it is a damp, drizzly November in my soul; whenever I find myself involuntarily pausing before coffin warehouses, and bringing up the rear of every funeral I meet; and especially whenever my hypos get such an upper hand of me, that it requires a strong moral principle to prevent me from deliberately stepping into the street, and methodically knocking people's hats off—then, I account it high time to get to sea as soon as I can. This is my substitute for pistol and ball. With a philosophical flourish Cato throws himself upon his sword; I quietly take to the ship. There is nothing surprising in this. If they but knew it, almost all men in their degree, some time or other, cherish very nearly the same feelings towards the ocean with me.",
                            style = com.lura.domain.engine.ReaderTextStyle.Body
                        )
                    )
                ),
                 Chapter(
                    title = "Chapter 2. The Carpet-Bag.",
                    elements = listOf(
                        com.lura.domain.engine.ReaderElement.Text(
                            content = "I stuffed a shirt or two into my old carpet-bag, tucked it under my arm, and started for Cape Horn and the Pacific. Quitting the good city of old Manhatto, I duly arrived in New Bedford. It was a Saturday night in December. Much was I disappointed upon learning that the little packet for Nantucket had already sailed, and that no way of reaching that place would offer, till the following Monday.",
                            style = com.lura.domain.engine.ReaderTextStyle.Body
                        )
                    )
                )
            )
        )
    }
}
