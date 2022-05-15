## 从客户端发送 From Client

### buy

消耗资金从商店中购买物品

- `shopItemIndex: Int` 要购买的物品在商店中的索引

### use

将仓库中的索引为itemIndex的物品用在索引为reactorIndex的反应堆的序号为slotNumber的物品槽

- `itemIndex: Int` 要使用的物品在仓库中的索引
- `reactorIndex: Int` 要使用的目标反应堆的索引
- `slotNumber: Int` 要使用的目标单元槽的序号

### sell

卖出仓库的物品

- `itemIndex: Int` 要卖出的物品在仓库中的索引

### turn

改变发电厂状态

- `status: Boolean` 目标状态

### reactorTurn

改变反应堆状态

- `reactorIndex: Int` 目标反应堆的索引
- `status: Boolean` 目标状态

### save

保存游戏

- `path: String` 存档路径

### load

载入游戏

- `path: String` 存档路径

### request

设置需求，相应的需求会在每次更新状态时候发给客户端

- `requestList: Request[]` 需求列表

```typescript
interface Request { id: String }
interface ReactorRequest extends Request { id: String, index: Int }
interface ReactorHistoryRequest extends Request { id: String; index: Int }
```

## 从服务端发送 From Server

```typescript

interface Pack {
    type: String;
}

interface ResponsePack() {
    type: "response";
    succeeded: Boolean;
    message: String;
}

interface StatePack { 
    type: "state";
    data: Response[];
}

interface Response {
    id: String;
    succeeded: Boolean;
    message: String;
    data: any;
}
```

其中ResponsePack在每次服务器收到指令后发出，StatePack在每次游戏内容更新后发出