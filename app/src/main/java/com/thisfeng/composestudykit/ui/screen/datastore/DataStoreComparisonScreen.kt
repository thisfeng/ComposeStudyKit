package com.thisfeng.composestudykit.ui.screen.datastore

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thisfeng.composestudykit.ui.viewmodel.DataStoreComparisonViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataStoreComparisonScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel = remember { DataStoreComparisonViewModel(context) }
    
    val dataStoreStats by viewModel.dataStoreStats.collectAsState()
    val operationStatus by viewModel.operationStatus.collectAsState()
    val testData by viewModel.testData.collectAsState()
    
    Column(modifier = Modifier.fillMaxSize()) {
        // 顶部标题栏
        TopAppBar(
            title = { 
                Text(
                    text = "DataStore 对比",
                    fontWeight = FontWeight.Bold
                ) 
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "返回"
                    )
                }
            }
        )
        
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "DataStore 缓存演示",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            if (operationStatus.isNotEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text(
                            text = operationStatus,
                            modifier = Modifier.padding(16.dp),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            item {
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("操作控制", fontWeight = FontWeight.Bold)
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.testDataStoreWrite() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("写入测试", fontSize = 12.sp)
                            }
                            Button(
                                onClick = { viewModel.testDataStoreRead() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("读取测试", fontSize = 12.sp)
                            }
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.testBatchWrite() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("批量写入", fontSize = 12.sp)
                            }
                            OutlinedButton(
                                onClick = { viewModel.testConcurrentPerformance() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("并发测试", fontSize = 12.sp)
                            }
                        }
                        
                        FilledTonalButton(
                            onClick = { viewModel.clearAllCaches() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("清空缓存")
                        }
                    }
                }
            }
            
            dataStoreStats?.let { stats ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("DataStore 缓存统计", fontWeight = FontWeight.Bold)
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("总缓存键数:")
                                Text("${stats.totalKeys}")
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("存储大小:")
                                Text("${stats.totalSize} bytes")
                            }
                        }
                    }
                }
            }
            
            testData?.let { data ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("测试数据", fontWeight = FontWeight.Bold)
                            Text(
                                text = data,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
            
            item {
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("DataStore 优势", fontWeight = FontWeight.Bold)
                        
                        listOf(
                            "🚀 异步操作，不阻塞 UI 线程",
                            "🔒 类型安全，支持 Kotlin 协程",
                            "⚡ 更好的性能和内存管理",
                            "🛡️ 事务性操作，保证数据一致性",
                            "🔄 原生支持 Flow 和响应式编程"
                        ).forEach { advantage ->
                            Text(
                                text = advantage,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// 添加 Preview
@Preview(showBackground = true)
@Composable
private fun DataStoreComparisonScreenPreview() {
    DataStoreComparisonScreen(
        onBackClick = {}
    )
}
