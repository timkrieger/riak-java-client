/*
 * Copyright 2013 Basho Technologies Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.basho.riak.client.core.operations;

import com.basho.riak.client.core.FutureOperation;
import com.basho.riak.client.core.RiakMessage;
import com.basho.riak.client.query.BucketProperties;
import com.basho.riak.client.query.functions.Function;
import com.basho.riak.client.util.ByteArrayWrapper;
import com.basho.riak.client.util.RiakMessageCodes;
import com.basho.riak.protobuf.RiakPB;
import com.basho.riak.protobuf.RiakPB.RpbModFun;
import com.google.protobuf.ByteString;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 *
 * @author Brian Roach <roach at basho dot com>
 * @since 2.0
 */
//TODO: return some sort of "success" instead of Void
public class StoreBucketPropsOperation extends FutureOperation<Void, Void>
{
    private final ByteArrayWrapper bucketName;
    private ByteArrayWrapper bucketType;
    private final BucketProperties bucketProperties;
    
    public StoreBucketPropsOperation(ByteArrayWrapper bucketName, BucketProperties properties)
    {
        if (null == bucketName || bucketName.length() == 0)
        {
            throw new IllegalArgumentException("Bucket name cannot be null or zero length.");
        }
        if (null == properties)
        {
            throw new IllegalArgumentException("Bucket properties cannot be null.");
        }
        
        this.bucketName = bucketName;
        this.bucketProperties = properties;
    }
    
    /**
     * Set the bucket type.
     * If unset "default" is used. 
     * @param bucketType the bucket type to use
     * @return A reference to this object.
     */
    public StoreBucketPropsOperation withBucketType(ByteArrayWrapper bucketType)
    {
        if (null == bucketType || bucketType.length() == 0)
        {
            throw new IllegalArgumentException("Bucket type can not be null or zero length");
        }
        this.bucketType = bucketType;
        return this;
    }
    
    @Override
    protected Void convert(List<Void> rawResponse) throws ExecutionException
    {
        return null;
    }

    @Override
    protected RiakMessage createChannelMessage()
    {
        RiakPB.RpbBucketProps.Builder propsBuilder =
            RiakPB.RpbBucketProps.newBuilder();
        
        if (bucketProperties.hasNVal())
        {
            propsBuilder.setNVal(bucketProperties.getNVal());
        }
        if (bucketProperties.hasAllowMulti())
        {
            propsBuilder.setAllowMult(bucketProperties.getAllowMulti());
        }
        if (bucketProperties.hasLastWriteWins())
        {
            propsBuilder.setLastWriteWins(bucketProperties.getLastWriteWins());
        }
        if (bucketProperties.hasPrecommitHooks())
        {
            propsBuilder.addAllPrecommit(convertHooks(bucketProperties.getPrecommitHooks()));
        }
        if (bucketProperties.hasPostcommitHooks())
        {
            propsBuilder.addAllPostcommit(convertHooks(bucketProperties.getPostcommitHooks()));
        }
        if (bucketProperties.hasChashKeyFunction())
        {
            propsBuilder.setChashKeyfun(convertModFun(bucketProperties.getChashKeyFunction()));
        }
        if (bucketProperties.hasLinkwalkFunction())
        {
            propsBuilder.setLinkfun(convertModFun(bucketProperties.getLinkwalkFunction()));
        }
        if (bucketProperties.hasOldVClock())
        {
            propsBuilder.setOldVclock(bucketProperties.getOldVClock().intValue());
        }
        if (bucketProperties.hasYoungVClock())
        {
            propsBuilder.setYoungVclock(bucketProperties.getYoungVClock().intValue());
        }
        if (bucketProperties.hasBigVClock())
        {
            propsBuilder.setBigVclock(bucketProperties.getBigVClock().intValue());
        }
        if (bucketProperties.hasSmallVClock())
        {
            propsBuilder.setSmallVclock(bucketProperties.getSmallVClock().intValue());
        }
        if (bucketProperties.hasPr())
        {
            propsBuilder.setPr(bucketProperties.getPr().getIntValue());
        }
        if (bucketProperties.hasR())
        {
            propsBuilder.setR(bucketProperties.getR().getIntValue());
        }
        if (bucketProperties.hasW())
        {
            propsBuilder.setW(bucketProperties.getW().getIntValue());
        }
        if (bucketProperties.hasPw())
        {
            propsBuilder.setPw(bucketProperties.getPw().getIntValue());
        }
        if (bucketProperties.hasDw())
        {
            propsBuilder.setDw(bucketProperties.getDw().getIntValue());
        }
        if (bucketProperties.hasRw())
        {
            propsBuilder.setRw(bucketProperties.getRw().getIntValue());
        }
        if (bucketProperties.hasBasicQuorum())
        {
            propsBuilder.setBasicQuorum(bucketProperties.getBasicQuorum());
        }
        if (bucketProperties.hasNotFoundOk())
        {
            propsBuilder.setNotfoundOk(bucketProperties.getNotFoundOk());
        }
        if (bucketProperties.hasBackend())
        {
            propsBuilder.setBackend(ByteString.copyFromUtf8(bucketProperties.getBackend()));
        }
        if (bucketProperties.hasRiakSearchEnabled())
        {
            propsBuilder.setSearch(bucketProperties.getRiakSearchEnabled());
        }
        if (bucketProperties.hasYokozunaIndex())
        {
            propsBuilder.setYzIndex(ByteString.copyFromUtf8(bucketProperties.getYokozunaIndex()));
        }
        
        RiakPB.RpbSetBucketReq.Builder builder = 
            RiakPB.RpbSetBucketReq.newBuilder();
        
        if (bucketType != null)
        {
            builder.setType(ByteString.copyFrom(bucketType.unsafeGetValue()));
        }
        
        RiakPB.RpbSetBucketReq req = 
            builder.setBucket(ByteString.copyFrom(bucketName.unsafeGetValue()))
                   .setProps(propsBuilder).build();
        
        return new RiakMessage(RiakMessageCodes.MSG_SetBucketReq, req.toByteArray());
        
        
    }

    @Override
    protected Void decode(RiakMessage rawMessage)
    {
        Operations.checkMessageType(rawMessage, RiakMessageCodes.MSG_SetBucketResp);
        return null;
    }
    
    private RpbModFun convertModFun(Function f)
    {
        return RpbModFun.newBuilder()
                    .setModule(ByteString.copyFromUtf8(f.getModule()))
                    .setFunction(ByteString.copyFromUtf8(f.getFunction()))
                    .build();
    }
    
    private List<RiakPB.RpbCommitHook> convertHooks(List<Function> hookList) {
        List<RiakPB.RpbCommitHook> pbHookList = new ArrayList<RiakPB.RpbCommitHook>(hookList.size());
        RiakPB.RpbCommitHook.Builder builder = RiakPB.RpbCommitHook.newBuilder();
        RiakPB.RpbModFun.Builder mfBuilder = RiakPB.RpbModFun.newBuilder();
        for (Function hook : hookList) {
            if (hook.isJavascript()) {
                builder.setName(ByteString.copyFromUtf8(hook.getName()));
            } else {
                mfBuilder.setModule(ByteString.copyFromUtf8(hook.getModule()));
                mfBuilder.setFunction(ByteString.copyFromUtf8(hook.getFunction()));
                builder.setModfun(mfBuilder);
            }
            
            pbHookList.add(builder.build());
            builder.clear();
            mfBuilder.clear();
        }
        return pbHookList;
    }
}
