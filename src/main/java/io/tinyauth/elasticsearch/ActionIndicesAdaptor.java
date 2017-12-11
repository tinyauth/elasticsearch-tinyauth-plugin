/*
 * Copyright 2017 tinyauth.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.tinyauth.elasticsearch;

import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.logging.Loggers;
import org.json.JSONStringer;
import org.guavaberry.collections.DefaultHashMap;

import org.elasticsearch.action.ActionRequest;

import org.elasticsearch.action.admin.cluster.allocation.ClusterAllocationExplainRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.node.hotthreads.NodesHotThreadsRequest;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoRequest;
import org.elasticsearch.action.admin.cluster.node.liveness.LivenessRequest;
import org.elasticsearch.action.admin.cluster.node.stats.NodesStatsRequest;
import org.elasticsearch.action.admin.cluster.node.tasks.cancel.CancelTasksRequest;
import org.elasticsearch.action.admin.cluster.node.tasks.get.GetTaskRequest;
import org.elasticsearch.action.admin.cluster.node.tasks.list.ListTasksRequest;
import org.elasticsearch.action.admin.cluster.remote.RemoteInfoRequest;
import org.elasticsearch.action.admin.cluster.repositories.delete.DeleteRepositoryRequest;
import org.elasticsearch.action.admin.cluster.repositories.get.GetRepositoriesRequest;
import org.elasticsearch.action.admin.cluster.repositories.put.PutRepositoryRequest;
import org.elasticsearch.action.admin.cluster.repositories.verify.VerifyRepositoryRequest;
import org.elasticsearch.action.admin.cluster.reroute.ClusterRerouteRequest;
import org.elasticsearch.action.admin.cluster.settings.ClusterUpdateSettingsRequest;
import org.elasticsearch.action.admin.cluster.shards.ClusterSearchShardsRequest;
import org.elasticsearch.action.admin.cluster.snapshots.create.CreateSnapshotRequest;
import org.elasticsearch.action.admin.cluster.snapshots.delete.DeleteSnapshotRequest;
import org.elasticsearch.action.admin.cluster.snapshots.get.GetSnapshotsRequest;
import org.elasticsearch.action.admin.cluster.snapshots.restore.RestoreSnapshotRequest;
import org.elasticsearch.action.admin.cluster.snapshots.status.SnapshotsStatusRequest;
import org.elasticsearch.action.admin.cluster.snapshots.status.TransportNodesSnapshotsStatus;
import org.elasticsearch.action.admin.cluster.state.ClusterStateRequest;
import org.elasticsearch.action.admin.cluster.stats.ClusterStatsRequest;
import org.elasticsearch.action.admin.cluster.storedscripts.DeleteStoredScriptRequest;
import org.elasticsearch.action.admin.cluster.storedscripts.GetStoredScriptRequest;
import org.elasticsearch.action.admin.cluster.storedscripts.PutStoredScriptRequest;
import org.elasticsearch.action.admin.cluster.tasks.PendingClusterTasksRequest;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeRequest;
import org.elasticsearch.action.admin.indices.cache.clear.ClearIndicesCacheRequest;
import org.elasticsearch.action.admin.indices.close.CloseIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.types.TypesExistsRequest;
import org.elasticsearch.action.admin.indices.flush.FlushRequest;
import org.elasticsearch.action.admin.indices.flush.ShardFlushRequest;
import org.elasticsearch.action.admin.indices.flush.SyncedFlushRequest;
import org.elasticsearch.action.admin.indices.forcemerge.ForceMergeRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetFieldMappingsIndexRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetFieldMappingsRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.admin.indices.open.OpenIndexRequest;
import org.elasticsearch.action.admin.indices.recovery.RecoveryRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.admin.indices.rollover.RolloverRequest;
import org.elasticsearch.action.admin.indices.segments.IndicesSegmentsRequest;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsRequest;
import org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsRequest;
import org.elasticsearch.action.admin.indices.shards.IndicesShardStoresRequest;
import org.elasticsearch.action.admin.indices.shrink.ShrinkRequest;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsRequest;
import org.elasticsearch.action.admin.indices.template.delete.DeleteIndexTemplateRequest;
import org.elasticsearch.action.admin.indices.template.get.GetIndexTemplatesRequest;
import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateRequest;
import org.elasticsearch.action.admin.indices.upgrade.get.UpgradeStatusRequest;
import org.elasticsearch.action.admin.indices.upgrade.post.UpgradeRequest;
import org.elasticsearch.action.admin.indices.upgrade.post.UpgradeSettingsRequest;
import org.elasticsearch.action.admin.indices.validate.query.ValidateQueryRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkShardRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.explain.ExplainRequest;
import org.elasticsearch.action.fieldcaps.FieldCapabilitiesIndexRequest;
import org.elasticsearch.action.fieldcaps.FieldCapabilitiesRequest;
import org.elasticsearch.action.fieldstats.FieldStatsRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.get.MultiGetShardRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.ingest.DeletePipelineRequest;
import org.elasticsearch.action.ingest.GetPipelineRequest;
import org.elasticsearch.action.ingest.PutPipelineRequest;
import org.elasticsearch.action.ingest.SimulatePipelineRequest;
import org.elasticsearch.action.main.MainRequest;
import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.action.support.broadcast.BroadcastRequest;
import org.elasticsearch.action.support.master.AcknowledgedRequest;
import org.elasticsearch.action.support.master.MasterNodeReadRequest;
import org.elasticsearch.action.support.master.MasterNodeRequest;
import org.elasticsearch.action.support.master.info.ClusterInfoRequest;
import org.elasticsearch.action.support.nodes.BaseNodesRequest;
import org.elasticsearch.action.support.replication.BasicReplicationRequest;
import org.elasticsearch.action.support.replication.ReplicatedWriteRequest;
import org.elasticsearch.action.support.replication.ReplicationRequest;
import org.elasticsearch.action.support.single.instance.InstanceShardOperationRequest;
import org.elasticsearch.action.support.single.shard.SingleShardRequest;
import org.elasticsearch.action.support.tasks.BaseTasksRequest;
import org.elasticsearch.action.termvectors.MultiTermVectorsRequest;
import org.elasticsearch.action.termvectors.MultiTermVectorsShardRequest;
import org.elasticsearch.action.termvectors.TermVectorsRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.gateway.TransportNodesListGatewayMetaState;
import org.elasticsearch.gateway.TransportNodesListGatewayStartedShards;
import org.elasticsearch.index.reindex.AbstractBulkByScrollRequest;
import org.elasticsearch.index.reindex.AbstractBulkIndexByScrollRequest;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.index.reindex.ReindexRequest;
import org.elasticsearch.index.reindex.UpdateByQueryRequest;
import org.elasticsearch.indices.store.TransportNodesListShardStoreMetaData;


interface PermissionExtractor {
  void extract(Map<String, Set<String>> permissions, ActionRequest request);
}


public class ActionIndicesAdaptor {
  private static final Logger logger = Loggers.getLogger(ActionIndicesAdaptor.class);

  private String partition;
  private String service;
  private String region;

  private HashMap<Class<?>, PermissionExtractor> methods;

  private String formatArn() {
    return String.join(":",
      "arn",
      partition,
      service,
      region,
      ""
    );
  }

  private String formatArn(String resourceType, String resource) {
    return String.join(":",
      formatArn(),
      resourceType + "/" + resource
    );
  }

  private String formatArn(String resourceType) {
    return formatArn(resourceType, "");
  }

  public ActionIndicesAdaptor(String partition, String service, String region) {
    this.partition = partition;
    this.service = service;
    this.region = region;

    this.methods = new HashMap<>();

    /* ClusterAllocationExplainRequest */
    this.methods.put(ClusterAllocationExplainRequest.class, (permissions, request) -> {
      ClusterAllocationExplainRequest req = (ClusterAllocationExplainRequest)request;
      Set<String> permission = permissions.get("ClusterMonitorAllocationExplain");
      /* this index related request has an getIndex() method */
      permission.add(formatArn("index", req.getIndex()));

    });

    /* ClusterHealthRequest */
    this.methods.put(ClusterHealthRequest.class, (permissions, request) -> {
      ClusterHealthRequest req = (ClusterHealthRequest)request;
      Set<String> permission = permissions.get("ClusterMonitorHealth");
      /* this index related request has an indices() method */
      Stream.of(req.indices()).map(idx -> formatArn("index", idx)).forEach(permission::add);

    });

    // Method rejected due to return type: int
    /* NodesHotThreadsRequest */
    this.methods.put(NodesHotThreadsRequest.class, (permissions, request) -> {
      NodesHotThreadsRequest req = (NodesHotThreadsRequest)request;
      Set<String> permission = permissions.get("ClusterMonitorNodesHotThreads");
      /* WARNING: No particular resource types were identified */
      permission.add(formatArn());

    });

    // Method rejected due to return type: boolean
    /* NodesInfoRequest */
    this.methods.put(NodesInfoRequest.class, (permissions, request) -> {
      NodesInfoRequest req = (NodesInfoRequest)request;
      Set<String> permission = permissions.get("ClusterMonitorNodesInfo");
      /* WARNING: No particular resource types were identified */
      permission.add(formatArn());

    });

    // Method rejected due to return type: org.elasticsearch.action.admin.indices.stats.CommonStatsFlags
    /* NodesStatsRequest */
    this.methods.put(NodesStatsRequest.class, (permissions, request) -> {
      NodesStatsRequest req = (NodesStatsRequest)request;
      Set<String> permission = permissions.get("ClusterMonitorNodesStats");
      /* WARNING: No particular resource types were identified */
      permission.add(formatArn());

    });

    /* CancelTasksRequest */
    this.methods.put(CancelTasksRequest.class, (permissions, request) -> {
      CancelTasksRequest req = (CancelTasksRequest)request;
      Set<String> permission = permissions.get("ClusterAdminTasksCancel");
      /* this node related request has an getNodes() method */
      Stream.of(req.getNodes()).map(idx -> formatArn("node", idx)).forEach(permission::add);

    });

    /* GetTaskRequest */
    this.methods.put(GetTaskRequest.class, (permissions, request) -> {
      GetTaskRequest req = (GetTaskRequest)request;
      Set<String> permission = permissions.get("ClusterMonitorTaskGet");
      /* WARNING: No particular resource types were identified */
      permission.add(formatArn());

    });

    /* ListTasksRequest */
    this.methods.put(ListTasksRequest.class, (permissions, request) -> {
      ListTasksRequest req = (ListTasksRequest)request;
      Set<String> permission = permissions.get("ClusterMonitorTasksLists");
      /* this node related request has an getNodes() method */
      Stream.of(req.getNodes()).map(idx -> formatArn("node", idx)).forEach(permission::add);

    });

    /* RemoteInfoRequest */
    this.methods.put(RemoteInfoRequest.class, (permissions, request) -> {
      RemoteInfoRequest req = (RemoteInfoRequest)request;
      Set<String> permission = permissions.get("ClusterMonitorRemoteInfo");
      /* WARNING: No particular resource types were identified */
      permission.add(formatArn());

    });

    /* DeleteRepositoryRequest */
    this.methods.put(DeleteRepositoryRequest.class, (permissions, request) -> {
      DeleteRepositoryRequest req = (DeleteRepositoryRequest)request;
      Set<String> permission = permissions.get("ClusterAdminRepositoryDelete");
      /* this repository related request has an name() method */
      permission.add(formatArn("repository", req.name()));

    });

    /* GetRepositoriesRequest */
    this.methods.put(GetRepositoriesRequest.class, (permissions, request) -> {
      GetRepositoriesRequest req = (GetRepositoriesRequest)request;
      Set<String> permission = permissions.get("ClusterAdminRepositoryGet");
      /* this repository related request has an repositories() method */
      Stream.of(req.repositories()).map(idx -> formatArn("repository", idx)).forEach(permission::add);

    });

    /* PutRepositoryRequest */
    this.methods.put(PutRepositoryRequest.class, (permissions, request) -> {
      PutRepositoryRequest req = (PutRepositoryRequest)request;
      Set<String> permission = permissions.get("ClusterAdminRepositoryPut");
      /* this repository related request has an name() method */
      permission.add(formatArn("repository", req.name()));

    });

    /* VerifyRepositoryRequest */
    this.methods.put(VerifyRepositoryRequest.class, (permissions, request) -> {
      VerifyRepositoryRequest req = (VerifyRepositoryRequest)request;
      Set<String> permission = permissions.get("ClusterAdminRepositoryVerify");
      /* this repository related request has an name() method */
      permission.add(formatArn("repository", req.name()));

    });

    /* ClusterRerouteRequest */
    this.methods.put(ClusterRerouteRequest.class, (permissions, request) -> {
      ClusterRerouteRequest req = (ClusterRerouteRequest)request;
      Set<String> permission = permissions.get("ClusterAdminReroute");
      /* WARNING: No particular resource types were identified */
      permission.add(formatArn());

    });

    /* ClusterUpdateSettingsRequest */
    this.methods.put(ClusterUpdateSettingsRequest.class, (permissions, request) -> {
      ClusterUpdateSettingsRequest req = (ClusterUpdateSettingsRequest)request;
      Set<String> permission = permissions.get("ClusterAdminSettingsUpdate");
      /* WARNING: No particular resource types were identified */
      permission.add(formatArn());

    });

    /* ClusterSearchShardsRequest */
    this.methods.put(ClusterSearchShardsRequest.class, (permissions, request) -> {
      ClusterSearchShardsRequest req = (ClusterSearchShardsRequest)request;
      Set<String> permission = permissions.get("IndicesAdminShardsSearchShards");
      /* this index related request has an indices() method */
      Stream.of(req.indices()).map(idx -> formatArn("index", idx)).forEach(permission::add);

    });

    /* CreateSnapshotRequest */
    this.methods.put(CreateSnapshotRequest.class, (permissions, request) -> {
      CreateSnapshotRequest req = (CreateSnapshotRequest)request;
      Set<String> permission = permissions.get("ClusterAdminSnapshotCreate");
      /* this index related request has an indices() method */
      Stream.of(req.indices()).map(idx -> formatArn("index", idx)).forEach(permission::add);

      /* this snapshot related request has an snapshot() method */
      permission.add(formatArn("snapshot", req.snapshot()));

    });

    /* DeleteSnapshotRequest */
    this.methods.put(DeleteSnapshotRequest.class, (permissions, request) -> {
      DeleteSnapshotRequest req = (DeleteSnapshotRequest)request;
      Set<String> permission = permissions.get("ClusterAdminSnapshotDelete");
      /* this snapshot related request has an snapshot() method */
      permission.add(formatArn("snapshot", req.snapshot()));

    });

    /* GetSnapshotsRequest */
    this.methods.put(GetSnapshotsRequest.class, (permissions, request) -> {
      GetSnapshotsRequest req = (GetSnapshotsRequest)request;
      Set<String> permission = permissions.get("ClusterAdminSnapshotGet");
      /* this snapshot related request has an snapshots() method */
      Stream.of(req.snapshots()).map(idx -> formatArn("snapshot", idx)).forEach(permission::add);

    });

    /* RestoreSnapshotRequest */
    this.methods.put(RestoreSnapshotRequest.class, (permissions, request) -> {
      RestoreSnapshotRequest req = (RestoreSnapshotRequest)request;
      Set<String> permission = permissions.get("ClusterAdminSnapshotRestore");
      /* this index related request has an indices() method */
      Stream.of(req.indices()).map(idx -> formatArn("index", idx)).forEach(permission::add);

      /* this snapshot related request has an snapshot() method */
      permission.add(formatArn("snapshot", req.snapshot()));

    });

    /* SnapshotsStatusRequest */
    this.methods.put(SnapshotsStatusRequest.class, (permissions, request) -> {
      SnapshotsStatusRequest req = (SnapshotsStatusRequest)request;
      Set<String> permission = permissions.get("ClusterAdminSnapshotStatus");
      /* this snapshot related request has an snapshots() method */
      Stream.of(req.snapshots()).map(idx -> formatArn("snapshot", idx)).forEach(permission::add);

    });

    /* ClusterStateRequest */
    this.methods.put(ClusterStateRequest.class, (permissions, request) -> {
      ClusterStateRequest req = (ClusterStateRequest)request;
      Set<String> permission = permissions.get("ClusterMonitorState");
      /* this index related request has an indices() method */
      Stream.of(req.indices()).map(idx -> formatArn("index", idx)).forEach(permission::add);

    });

    /* ClusterStatsRequest */
    this.methods.put(ClusterStatsRequest.class, (permissions, request) -> {
      ClusterStatsRequest req = (ClusterStatsRequest)request;
      Set<String> permission = permissions.get("ClusterMonitorStats");
      /* WARNING: No particular resource types were identified */
      permission.add(formatArn());

    });

    /* DeleteStoredScriptRequest */
    this.methods.put(DeleteStoredScriptRequest.class, (permissions, request) -> {
      DeleteStoredScriptRequest req = (DeleteStoredScriptRequest)request;
      Set<String> permission = permissions.get("ClusterAdminScriptDelete");
      /* this stored-script related request has an id() method */
      permission.add(formatArn("stored-script", req.id()));

    });

    /* GetStoredScriptRequest */
    this.methods.put(GetStoredScriptRequest.class, (permissions, request) -> {
      GetStoredScriptRequest req = (GetStoredScriptRequest)request;
      Set<String> permission = permissions.get("ClusterAdminScriptGet");
      /* this stored-script related request has an id() method */
      permission.add(formatArn("stored-script", req.id()));

    });

    /* PutStoredScriptRequest */
    this.methods.put(PutStoredScriptRequest.class, (permissions, request) -> {
      PutStoredScriptRequest req = (PutStoredScriptRequest)request;
      Set<String> permission = permissions.get("ClusterAdminScriptPut");
      /* this stored-script related request has an id() method */
      permission.add(formatArn("stored-script", req.id()));

    });

    /* PendingClusterTasksRequest */
    this.methods.put(PendingClusterTasksRequest.class, (permissions, request) -> {
      PendingClusterTasksRequest req = (PendingClusterTasksRequest)request;
      Set<String> permission = permissions.get("ClusterMonitorTask");
      /* WARNING: No particular resource types were identified */
      permission.add(formatArn());

    });

    /* IndicesAliasesRequest */
    this.methods.put(IndicesAliasesRequest.class, (permissions, request) -> {
      IndicesAliasesRequest req = (IndicesAliasesRequest)request;
      Set<String> permission = permissions.get("IndicesAdminAliases");
      /* WARNING: No particular resource types were identified */
      permission.add(formatArn());

    });

    /* GetAliasesRequest */
    this.methods.put(GetAliasesRequest.class, (permissions, request) -> {
      GetAliasesRequest req = (GetAliasesRequest)request;
      Set<String> permission = permissions.get("IndicesAdminAliasesExists");
      /* this index related request has an indices() method */
      Stream.of(req.indices()).map(idx -> formatArn("index", idx)).forEach(permission::add);

    });

    /* GetAliasesRequest */
    this.methods.put(GetAliasesRequest.class, (permissions, request) -> {
      GetAliasesRequest req = (GetAliasesRequest)request;
      Set<String> permission = permissions.get("IndicesAdminAliasesGet");
      /* this index related request has an indices() method */
      Stream.of(req.indices()).map(idx -> formatArn("index", idx)).forEach(permission::add);

    });

    /* AnalyzeRequest */
    this.methods.put(AnalyzeRequest.class, (permissions, request) -> {
      AnalyzeRequest req = (AnalyzeRequest)request;
      Set<String> permission = permissions.get("IndicesAdminAnalyze");
      /* this index related request has an indices() method */
      Stream.of(req.indices()).map(idx -> formatArn("index", idx)).forEach(permission::add);

    });

    /* ClearIndicesCacheRequest */
    this.methods.put(ClearIndicesCacheRequest.class, (permissions, request) -> {
      ClearIndicesCacheRequest req = (ClearIndicesCacheRequest)request;
      Set<String> permission = permissions.get("IndicesAdminCacheClear");
      /* this index related request has an indices() method */
      Stream.of(req.indices()).map(idx -> formatArn("index", idx)).forEach(permission::add);

    });

    /* CloseIndexRequest */
    this.methods.put(CloseIndexRequest.class, (permissions, request) -> {
      CloseIndexRequest req = (CloseIndexRequest)request;
      Set<String> permission = permissions.get("IndicesAdminClose");
      /* this index related request has an indices() method */
      Stream.of(req.indices()).map(idx -> formatArn("index", idx)).forEach(permission::add);

    });

    /* CreateIndexRequest */
    this.methods.put(CreateIndexRequest.class, (permissions, request) -> {
      CreateIndexRequest req = (CreateIndexRequest)request;
      Set<String> permission = permissions.get("IndicesAdminCreate");
      /* this index related request has an indices() method */
      Stream.of(req.indices()).map(idx -> formatArn("index", idx)).forEach(permission::add);

    });

    /* DeleteIndexRequest */
    this.methods.put(DeleteIndexRequest.class, (permissions, request) -> {
      DeleteIndexRequest req = (DeleteIndexRequest)request;
      Set<String> permission = permissions.get("IndicesAdminDelete");
      /* this index related request has an indices() method */
      Stream.of(req.indices()).map(idx -> formatArn("index", idx)).forEach(permission::add);

    });

    /* IndicesExistsRequest */
    this.methods.put(IndicesExistsRequest.class, (permissions, request) -> {
      IndicesExistsRequest req = (IndicesExistsRequest)request;
      Set<String> permission = permissions.get("IndicesAdminExists");
      /* this index related request has an indices() method */
      Stream.of(req.indices()).map(idx -> formatArn("index", idx)).forEach(permission::add);

    });

    /* TypesExistsRequest */
    this.methods.put(TypesExistsRequest.class, (permissions, request) -> {
      TypesExistsRequest req = (TypesExistsRequest)request;
      Set<String> permission = permissions.get("IndicesAdminTypesExists");
      /* this index related request has an indices() method */
      Stream.of(req.indices()).map(idx -> formatArn("index", idx)).forEach(permission::add);

    });

    /* FlushRequest */
    this.methods.put(FlushRequest.class, (permissions, request) -> {
      FlushRequest req = (FlushRequest)request;
      Set<String> permission = permissions.get("IndicesAdminFlush");
      /* this index related request has an indices() method */
      Stream.of(req.indices()).map(idx -> formatArn("index", idx)).forEach(permission::add);

    });

    /* SyncedFlushRequest */
    this.methods.put(SyncedFlushRequest.class, (permissions, request) -> {
      SyncedFlushRequest req = (SyncedFlushRequest)request;
      Set<String> permission = permissions.get("IndicesAdminSyncedFlush");
      /* this index related request has an indices() method */
      Stream.of(req.indices()).map(idx -> formatArn("index", idx)).forEach(permission::add);

    });

    /* ForceMergeRequest */
    this.methods.put(ForceMergeRequest.class, (permissions, request) -> {
      ForceMergeRequest req = (ForceMergeRequest)request;
      Set<String> permission = permissions.get("IndicesAdminForcemerge");
      /* this index related request has an indices() method */
      Stream.of(req.indices()).map(idx -> formatArn("index", idx)).forEach(permission::add);

    });

    /* GetIndexRequest */
    this.methods.put(GetIndexRequest.class, (permissions, request) -> {
      GetIndexRequest req = (GetIndexRequest)request;
      Set<String> permission = permissions.get("IndicesAdminGet");
      /* this index related request has an indices() method */
      Stream.of(req.indices()).map(idx -> formatArn("index", idx)).forEach(permission::add);

    });

    /* GetFieldMappingsRequest */
    this.methods.put(GetFieldMappingsRequest.class, (permissions, request) -> {
      GetFieldMappingsRequest req = (GetFieldMappingsRequest)request;
      Set<String> permission = permissions.get("IndicesAdminMappingsFieldsGet");
      /* this index related request has an indices() method */
      Stream.of(req.indices()).map(idx -> formatArn("index", idx)).forEach(permission::add);

    });

    /* GetMappingsRequest */
    this.methods.put(GetMappingsRequest.class, (permissions, request) -> {
      GetMappingsRequest req = (GetMappingsRequest)request;
      Set<String> permission = permissions.get("IndicesAdminMappingsGet");
      /* this index related request has an indices() method */
      Stream.of(req.indices()).map(idx -> formatArn("index", idx)).forEach(permission::add);

    });

    /* PutMappingRequest */
    this.methods.put(PutMappingRequest.class, (permissions, request) -> {
      PutMappingRequest req = (PutMappingRequest)request;
      Set<String> permission = permissions.get("IndicesAdminMappingPut");
      /* this index related request has an indices() method */
      Stream.of(req.indices()).map(idx -> formatArn("index", idx)).forEach(permission::add);

    });

    /* OpenIndexRequest */
    this.methods.put(OpenIndexRequest.class, (permissions, request) -> {
      OpenIndexRequest req = (OpenIndexRequest)request;
      Set<String> permission = permissions.get("IndicesAdminOpen");
      /* this index related request has an indices() method */
      Stream.of(req.indices()).map(idx -> formatArn("index", idx)).forEach(permission::add);

    });

    /* RecoveryRequest */
    this.methods.put(RecoveryRequest.class, (permissions, request) -> {
      RecoveryRequest req = (RecoveryRequest)request;
      Set<String> permission = permissions.get("IndicesMonitorRecovery");
      /* this index related request has an indices() method */
      Stream.of(req.indices()).map(idx -> formatArn("index", idx)).forEach(permission::add);

    });

    /* RefreshRequest */
    this.methods.put(RefreshRequest.class, (permissions, request) -> {
      RefreshRequest req = (RefreshRequest)request;
      Set<String> permission = permissions.get("IndicesAdminRefresh");
      /* this index related request has an indices() method */
      Stream.of(req.indices()).map(idx -> formatArn("index", idx)).forEach(permission::add);

    });

    /* RolloverRequest */
    this.methods.put(RolloverRequest.class, (permissions, request) -> {
      RolloverRequest req = (RolloverRequest)request;
      Set<String> permission = permissions.get("IndicesAdminRollover");
      /* this index related request has an indices() method */
      Stream.of(req.indices()).map(idx -> formatArn("index", idx)).forEach(permission::add);

    });

    /* IndicesSegmentsRequest */
    this.methods.put(IndicesSegmentsRequest.class, (permissions, request) -> {
      IndicesSegmentsRequest req = (IndicesSegmentsRequest)request;
      Set<String> permission = permissions.get("IndicesMonitorSegments");
      /* this index related request has an indices() method */
      Stream.of(req.indices()).map(idx -> formatArn("index", idx)).forEach(permission::add);

    });

    /* GetSettingsRequest */
    this.methods.put(GetSettingsRequest.class, (permissions, request) -> {
      GetSettingsRequest req = (GetSettingsRequest)request;
      Set<String> permission = permissions.get("IndicesMonitorSettingsGet");
      /* this index related request has an indices() method */
      Stream.of(req.indices()).map(idx -> formatArn("index", idx)).forEach(permission::add);

    });

    /* UpdateSettingsRequest */
    this.methods.put(UpdateSettingsRequest.class, (permissions, request) -> {
      UpdateSettingsRequest req = (UpdateSettingsRequest)request;
      Set<String> permission = permissions.get("IndicesAdminSettingsUpdate");
      /* this index related request has an indices() method */
      Stream.of(req.indices()).map(idx -> formatArn("index", idx)).forEach(permission::add);

    });

    /* IndicesShardStoresRequest */
    this.methods.put(IndicesShardStoresRequest.class, (permissions, request) -> {
      IndicesShardStoresRequest req = (IndicesShardStoresRequest)request;
      Set<String> permission = permissions.get("IndicesMonitorShardStores");
      /* this index related request has an indices() method */
      Stream.of(req.indices()).map(idx -> formatArn("index", idx)).forEach(permission::add);

    });

    /* ShrinkRequest */
    this.methods.put(ShrinkRequest.class, (permissions, request) -> {
      ShrinkRequest req = (ShrinkRequest)request;
      Set<String> permission = permissions.get("IndicesAdminShrink");
      /* this index related request has an indices() method */
      Stream.of(req.indices()).map(idx -> formatArn("index", idx)).forEach(permission::add);

    });

    /* IndicesStatsRequest */
    this.methods.put(IndicesStatsRequest.class, (permissions, request) -> {
      IndicesStatsRequest req = (IndicesStatsRequest)request;
      Set<String> permission = permissions.get("IndicesMonitorStats");
      /* this index related request has an indices() method */
      Stream.of(req.indices()).map(idx -> formatArn("index", idx)).forEach(permission::add);

    });

    /* DeleteIndexTemplateRequest */
    this.methods.put(DeleteIndexTemplateRequest.class, (permissions, request) -> {
      DeleteIndexTemplateRequest req = (DeleteIndexTemplateRequest)request;
      Set<String> permission = permissions.get("IndicesAdminTemplateDelete");
      /* this index-template related request has an name() method */
      permission.add(formatArn("index-template", req.name()));

    });

    /* GetIndexTemplatesRequest */
    this.methods.put(GetIndexTemplatesRequest.class, (permissions, request) -> {
      GetIndexTemplatesRequest req = (GetIndexTemplatesRequest)request;
      Set<String> permission = permissions.get("IndicesAdminTemplateGet");
      /* this index-template related request has an names() method */
      Stream.of(req.names()).map(idx -> formatArn("index-template", idx)).forEach(permission::add);

    });

    /* PutIndexTemplateRequest */
    this.methods.put(PutIndexTemplateRequest.class, (permissions, request) -> {
      PutIndexTemplateRequest req = (PutIndexTemplateRequest)request;
      Set<String> permission = permissions.get("IndicesAdminTemplatePut");
      /* this index related request has an indices() method */
      Stream.of(req.indices()).map(idx -> formatArn("index", idx)).forEach(permission::add);

      /* this index-template related request has an name() method */
      permission.add(formatArn("index-template", req.name()));

    });

    /* UpgradeStatusRequest */
    this.methods.put(UpgradeStatusRequest.class, (permissions, request) -> {
      UpgradeStatusRequest req = (UpgradeStatusRequest)request;
      Set<String> permission = permissions.get("IndicesMonitorUpgrade");
      /* this index related request has an indices() method */
      Stream.of(req.indices()).map(idx -> formatArn("index", idx)).forEach(permission::add);

    });

    /* UpgradeRequest */
    this.methods.put(UpgradeRequest.class, (permissions, request) -> {
      UpgradeRequest req = (UpgradeRequest)request;
      Set<String> permission = permissions.get("IndicesAdminUpgrade");
      /* this index related request has an indices() method */
      Stream.of(req.indices()).map(idx -> formatArn("index", idx)).forEach(permission::add);

    });

    /* UpgradeSettingsRequest */
    this.methods.put(UpgradeSettingsRequest.class, (permissions, request) -> {
      UpgradeSettingsRequest req = (UpgradeSettingsRequest)request;
      Set<String> permission = permissions.get("InternalIndicesAdminUpgrade");
      /* WARNING: No particular resource types were identified */
      permission.add(formatArn());

    });

    /* ValidateQueryRequest */
    this.methods.put(ValidateQueryRequest.class, (permissions, request) -> {
      ValidateQueryRequest req = (ValidateQueryRequest)request;
      Set<String> permission = permissions.get("IndicesAdminValidateQuery");
      /* this index related request has an indices() method */
      Stream.of(req.indices()).map(idx -> formatArn("index", idx)).forEach(permission::add);

    });

    // Method rejected due to return type: java.util.List<org.elasticsearch.action.DocWriteRequest>
    // Method rejected due to return type: java.util.List<org.elasticsearch.action.DocWriteRequest>
    /* BulkRequest */
    this.methods.put(BulkRequest.class, (permissions, request) -> {
      BulkRequest req = (BulkRequest)request;
      Set<String> permission = permissions.get("IndicesDataWriteBulk");
      /* this index related request has an requests() method */
      req.requests().stream().forEach(ir -> getIndices(permissions, (ActionRequest) ir));

    });

    /* DeleteRequest */
    this.methods.put(DeleteRequest.class, (permissions, request) -> {
      DeleteRequest req = (DeleteRequest)request;
      Set<String> permission = permissions.get("IndicesDataWriteDelete");
      /* this index related request has an indices() method */
      Stream.of(req.indices()).map(idx -> formatArn("index", idx)).forEach(permission::add);

    });

    /* ExplainRequest */
    this.methods.put(ExplainRequest.class, (permissions, request) -> {
      ExplainRequest req = (ExplainRequest)request;
      Set<String> permission = permissions.get("IndicesDataReadExplain");
      /* this index related request has an indices() method */
      Stream.of(req.indices()).map(idx -> formatArn("index", idx)).forEach(permission::add);

    });

    /* FieldCapabilitiesRequest */
    this.methods.put(FieldCapabilitiesRequest.class, (permissions, request) -> {
      FieldCapabilitiesRequest req = (FieldCapabilitiesRequest)request;
      Set<String> permission = permissions.get("IndicesDataReadFieldCaps");
      /* this index related request has an indices() method */
      Stream.of(req.indices()).map(idx -> formatArn("index", idx)).forEach(permission::add);

    });

    /* FieldStatsRequest */
    this.methods.put(FieldStatsRequest.class, (permissions, request) -> {
      FieldStatsRequest req = (FieldStatsRequest)request;
      Set<String> permission = permissions.get("IndicesDataReadFieldStats");
      /* this index related request has an indices() method */
      Stream.of(req.indices()).map(idx -> formatArn("index", idx)).forEach(permission::add);

    });

    /* GetRequest */
    this.methods.put(GetRequest.class, (permissions, request) -> {
      GetRequest req = (GetRequest)request;
      Set<String> permission = permissions.get("IndicesDataReadGet");
      /* this index related request has an indices() method */
      Stream.of(req.indices()).map(idx -> formatArn("index", idx)).forEach(permission::add);

    });

    /* MultiGetRequest */
    this.methods.put(MultiGetRequest.class, (permissions, request) -> {
      MultiGetRequest req = (MultiGetRequest)request;
      Set<String> permission = permissions.get("IndicesDataReadMget");
      /* this index request has an getItems() method */
      req.getItems().stream()
        .flatMap(val -> Stream.of(val.indices()))
        .map(val -> formatArn("index", val))
        .forEach(permission::add);

    });

    /* IndexRequest */
    this.methods.put(IndexRequest.class, (permissions, request) -> {
      IndexRequest req = (IndexRequest)request;
      Set<String> permission = permissions.get("IndicesDataWriteIndex");
      /* this index related request has an indices() method */
      Stream.of(req.indices()).map(idx -> formatArn("index", idx)).forEach(permission::add);

    });

    /* DeletePipelineRequest */
    this.methods.put(DeletePipelineRequest.class, (permissions, request) -> {
      DeletePipelineRequest req = (DeletePipelineRequest)request;
      Set<String> permission = permissions.get("ClusterAdminIngestPipelineDelete");
      /* this pipeline related request has an getId() method */
      permission.add(formatArn("pipeline", req.getId()));

    });

    /* GetPipelineRequest */
    this.methods.put(GetPipelineRequest.class, (permissions, request) -> {
      GetPipelineRequest req = (GetPipelineRequest)request;
      Set<String> permission = permissions.get("ClusterAdminIngestPipelineGet");
      /* this pipeline related request has an getIds() method */
      Stream.of(req.getIds()).map(idx -> formatArn("pipeline", idx)).forEach(permission::add);

    });

    /* PutPipelineRequest */
    this.methods.put(PutPipelineRequest.class, (permissions, request) -> {
      PutPipelineRequest req = (PutPipelineRequest)request;
      Set<String> permission = permissions.get("ClusterAdminIngestPipelinePut");
      /* this pipeline related request has an getId() method */
      permission.add(formatArn("pipeline", req.getId()));

    });

    /* SimulatePipelineRequest */
    this.methods.put(SimulatePipelineRequest.class, (permissions, request) -> {
      SimulatePipelineRequest req = (SimulatePipelineRequest)request;
      Set<String> permission = permissions.get("ClusterAdminIngestPipelineSimulate");
      /* this pipeline related request has an getId() method */
      permission.add(formatArn("pipeline", req.getId()));

    });

    /* MainRequest */
    this.methods.put(MainRequest.class, (permissions, request) -> {
      MainRequest req = (MainRequest)request;
      Set<String> permission = permissions.get("ClusterMonitorMain");
      /* WARNING: No particular resource types were identified */
      permission.add(formatArn());

    });

    /* ClearScrollRequest */
    this.methods.put(ClearScrollRequest.class, (permissions, request) -> {
      ClearScrollRequest req = (ClearScrollRequest)request;
      Set<String> permission = permissions.get("IndicesDataReadScrollClear");
      /* WARNING: No particular resource types were identified */
      permission.add(formatArn());

    });

    // Method rejected due to return type: java.util.List<org.elasticsearch.action.search.SearchRequest>
    // Method rejected due to return type: java.util.List<org.elasticsearch.action.search.SearchRequest>
    /* MultiSearchRequest */
    this.methods.put(MultiSearchRequest.class, (permissions, request) -> {
      MultiSearchRequest req = (MultiSearchRequest)request;
      Set<String> permission = permissions.get("IndicesDataReadMsearch");
      /* this index request has an requests() method */
      req.requests().stream()
        .flatMap(val -> Stream.of(val.indices()))
        .map(val -> formatArn("index", val))
        .forEach(permission::add);

    });

    /* SearchRequest */
    this.methods.put(SearchRequest.class, (permissions, request) -> {
      SearchRequest req = (SearchRequest)request;
      Set<String> permission = permissions.get("IndicesDataReadSearch");
      /* this index related request has an indices() method */
      Stream.of(req.indices()).map(idx -> formatArn("index", idx)).forEach(permission::add);

    });

    /* SearchScrollRequest */
    this.methods.put(SearchScrollRequest.class, (permissions, request) -> {
      SearchScrollRequest req = (SearchScrollRequest)request;
      Set<String> permission = permissions.get("IndicesDataReadScroll");
      /* WARNING: No particular resource types were identified */
      permission.add(formatArn());

    });

    // Method rejected due to return type: java.util.List<org.elasticsearch.action.termvectors.TermVectorsRequest>
    /* MultiTermVectorsRequest */
    this.methods.put(MultiTermVectorsRequest.class, (permissions, request) -> {
      MultiTermVectorsRequest req = (MultiTermVectorsRequest)request;
      Set<String> permission = permissions.get("IndicesDataReadMtv");
      /* this index request has an getRequests() method */
      req.getRequests().stream()
        .flatMap(val -> Stream.of(val.indices()))
        .map(val -> formatArn("index", val))
        .forEach(permission::add);

    });

    /* TermVectorsRequest */
    this.methods.put(TermVectorsRequest.class, (permissions, request) -> {
      TermVectorsRequest req = (TermVectorsRequest)request;
      Set<String> permission = permissions.get("IndicesDataReadTv");
      /* this index related request has an indices() method */
      Stream.of(req.indices()).map(idx -> formatArn("index", idx)).forEach(permission::add);

    });

    /* UpdateRequest */
    this.methods.put(UpdateRequest.class, (permissions, request) -> {
      UpdateRequest req = (UpdateRequest)request;
      Set<String> permission = permissions.get("IndicesDataWriteUpdate");
      /* this index related request has an indices() method */
      Stream.of(req.indices()).map(idx -> formatArn("index", idx)).forEach(permission::add);

    });

    /* DeleteByQueryRequest */
    this.methods.put(DeleteByQueryRequest.class, (permissions, request) -> {
      DeleteByQueryRequest req = (DeleteByQueryRequest)request;
      Set<String> permission = permissions.get("IndicesDataWriteDeleteByquery");
      /* this index related request has an indices() method */
      Stream.of(req.indices()).map(idx -> formatArn("index", idx)).forEach(permission::add);

    });

    /* ReindexRequest */
    this.methods.put(ReindexRequest.class, (permissions, request) -> {
      ReindexRequest req = (ReindexRequest)request;
      Set<String> permission = permissions.get("IndicesDataWriteReindex");
      /* this index related request has an getDestination() method that returns an IndexRequest */
      Stream.of(req.getDestination().indices()).map(idx -> formatArn("index", idx)).forEach(permission::add);

    });

    /* UpdateByQueryRequest */
    this.methods.put(UpdateByQueryRequest.class, (permissions, request) -> {
      UpdateByQueryRequest req = (UpdateByQueryRequest)request;
      Set<String> permission = permissions.get("IndicesDataWriteUpdateByquery");
      /* this index related request has an indices() method */
      Stream.of(req.indices()).map(idx -> formatArn("index", idx)).forEach(permission::add);

    });
  }

  private void getIndices(Map<String, Set<String>>permissions, ActionRequest req) {
    PermissionExtractor extractor = methods.get(req.getClass());
    if (extractor == null) {
      logger.error("Unable to find adaptor for request " + req.getClass() + ". This is a bug!");
      return;
    }
    extractor.extract(permissions, req);
  }

  public Map<String, Set<String>> collectPermissions(ActionRequest req) {
    DefaultHashMap<String, Set<String>> permissions = new DefaultHashMap<>(() -> new HashSet<String>());
    getIndices(permissions, req);
    return permissions;
  }

  public void collectPermissions(ActionRequest req, JSONStringer stringer) {
    stringer.key("permit");
    stringer.object();

    for (Map.Entry<String, Set<String>> entry : collectPermissions(req).entrySet()) {
      stringer.key(entry.getKey());
      stringer.array();
      entry.getValue().stream().forEach(r -> stringer.value(r));
      stringer.endArray();
    }

    stringer.endObject();
  }
}
