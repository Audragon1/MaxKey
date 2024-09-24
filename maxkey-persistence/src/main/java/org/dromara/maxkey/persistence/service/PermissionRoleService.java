/*
 * Copyright [2024] [MaxKey of copyright http://www.maxkey.top]
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
 */
 


package org.dromara.maxkey.persistence.service;

import java.util.List;

import org.dromara.maxkey.entity.permissions.PermissionRole;
import org.dromara.maxkey.persistence.mapper.PermissionRoleMapper;
import org.dromara.mybatis.jpa.JpaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class PermissionRoleService  extends JpaService<PermissionRole>{
	static final Logger _logger = LoggerFactory.getLogger(PermissionRoleService.class);
   
    
	public PermissionRoleService() {
		super(PermissionRoleMapper.class);
	}

	@Override
	public PermissionRoleMapper getMapper() {
		return (PermissionRoleMapper)super.getMapper();
	}
	
	public boolean insertPermissionRoles(List<PermissionRole> permissionRolesList) {
	    return getMapper().insertPermissionRoles(permissionRolesList)>0;
	}
    
	public boolean deletePermissionRoles(List<PermissionRole> permissionRolesList) {
	     return getMapper().deletePermissionRoles(permissionRolesList)>=0;
	 }
	
    public List<PermissionRole> queryPermissionRoles(PermissionRole permissionRole){
        return getMapper().queryPermissionRoles(permissionRole);
    }    

}
