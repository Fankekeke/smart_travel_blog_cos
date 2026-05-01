<template>
  <div :class="[multipage === true ? 'multi-page':'single-page', 'not-menu-page', 'home-page']" style="background-color: #e9f8e7; border: none">
    <a-row class="head-info">
      <a-card class="head-info-card" style="width: 65%;margin: 0 auto">
        <a-col :span="16" style="margin-bottom: 15px">
          <!--          <div class="search-bar" v-show="!postDetailShow">-->
          <div style="padding: 30px 20px;background-color: #f1f1f1">
            <div class="search-bar">
              <a-checkbox v-model="searchVipFlag" class="search-checkbox">
                仅看VIP用户
              </a-checkbox>
              <a-input
                v-model="name"
                placeholder="输入作者名称"
                class="search-input"
                allow-clear />
              <a-input-search
                placeholder="搜索贴子关键字"
                class="search-input-search"
                @search="onSearch" />
            </div>
          </div>
        </a-col>
        <a-col :span="8" style="padding-left: 25px">
          <div class="head-info-count">
            <div class="head-info-welcome">
              {{welcomeMessage}}
            </div>
            <div class="head-info-desc" v-if="user.userId">
              <p>{{user.roleName ? user.roleName : '暂无角色'}}</p>
            </div>
            <div class="head-info-desc" v-else>
              <p><router-link to="/login">登录</router-link> 后可发帖、评论与收藏</p>
            </div>
            <div class="head-info-time" v-if="user.userId">上次登录时间：{{user.lastLoginTime ? user.lastLoginTime : '第一次访问系统'}}</div>
          </div>
          <div style="margin-top: 15px">
            <a-row class="more-info">
              <a-col :span="24">
                <a-button type="default" v-if="user.userId" style="margin-right: 8px;display: inline" @click="goManage">
                  进入后台
                </a-button>
                <a-button type="primary" v-if="user.userId && user.roleId != 76" @click="add" style="margin-right: 8px;display: inline">
                  发帖子
                </a-button>
              </a-col>
            </a-row>
          </div>
        </a-col>
      </a-card>
    </a-row>
    <a-row :gutter="20" style="width: 66%;margin: 0 auto;margin-bottom: 15px">
      <a-col :span="24">
        <a-carousel effect="fade">
          <div style="width: 100%;height: 350px" v-for="(item, index) in homeImage" :key="index"><img :src="'http://127.0.0.1:9527/imagesWeb/' + item" style="width: 100%;height: 100%;object-fit:cover;" /></div>
        </a-carousel>
      </a-col>
    </a-row>
    <a-row v-if="newsList.length > 0" style="width: 65%;margin: 0 auto;margin-bottom: 15px">
      <a-col :span="22">
        <a-alert
          banner
          :message="newsContent"
          type="info"
        />
      </a-col>
      <a-col :span="2">
        <a-button type="primary" style="margin-top: 2px;margin-left: 10px" @click="newsNext">下一页</a-button>
      </a-col>
    </a-row>
    <a-row :gutter="8" class="count-info">
      <a-card class="head-info-card" style="width: 65%;margin: 0 auto">
        <a-col :span="16">
          <a-tabs :activeKey="tabKey" tab-position="top" @change="tabChange">
            <a-tab-pane v-for="item in tagList" :key="item.id" :tab="item.name">
              <a-skeleton active v-if="loading" />
              <div v-if="!loading">
                <a-list item-layout="vertical" size="large" :pagination="pagination" :data-source="postList"  v-show="!postDetailShow">
                  <a-list-item slot="renderItem" key="item.title" slot-scope="item, index">
                    <template slot="actions">
                    <span key="message">
                      <a-icon type="message" style="margin-right: 8px" />
                      <span v-if="item.collect === 0">{{ item.reply }}</span>
                      <span v-else>{{ item.reply / item.collect }}</span> 回复
                    </span>
                      <span key="star">
                      <a-icon type="star" style="margin-right: 8px" />
                      {{ item.collect }} 收藏
                    </span>
                      <span key="to-top">
                      <a-icon type="to-top" style="margin-right: 8px" />
                      {{ timeFormat(item.createDate) }}
                    </span>
                    </template>
                    <a-list-item-meta :description="item.content.slice(0, 100) + '...'">
                      <div slot="title" class="post-title-wrapper">
                        <a @click="postReplyDetail(item)" class="post-title">{{ item.title }}</a>
                        <a-tag v-if="item.userLevel" :color="getUserLevelColor(item.userLevel)" class="user-level-tag">
                          {{ item.userLevel }}
                        </a-tag>
                      </div>
                      <div slot="avatar" class="avatar-wrapper">
                        <a-avatar shape="square" icon="user" :src="'http://127.0.0.1:9527/imagesWeb/' + item.userImages" />
                      </div>
                    </a-list-item-meta>
                  </a-list-item>
                </a-list>
              </div>
            </a-tab-pane>
          </a-tabs>
          <div v-if="postDetailShow && postDetail !== null" style="margin: 18px">
            <div style="margin-bottom: 10px">
              <a-breadcrumb>
                <a-breadcrumb-item><a @click="postDetailShow = false">返回</a></a-breadcrumb-item>
                <a-breadcrumb-item>{{ tabName }}</a-breadcrumb-item>
              </a-breadcrumb>
            </div>
            <p style="font-size: 22px;color: black;font-weight: 500;line-height: 150%;margin: 25px 50px;margin-top: 50px">
              {{ postDetail.title }}
              <a-icon type="form" style="cursor: pointer" v-if="user.userId === postDetail.userId" @click="edit(postDetail)"/>
            </p>
            <div style="margin: 25px 50px;font-size: 13px">
              <a-icon v-if="user.userId && user.roleId != 76 && collectUser === 0" type="heart" style="margin-right: 10px;cursor: pointer" @click="collectUserCheck(0)"/>
              <a-icon v-if="user.userId && user.roleId != 76 && collectUser > 0" type="heart" style="margin-right: 10px;color: red;cursor: pointer" @click="collectUserCheck(1)"/>
              <a @click="pushToDetail(postDetail.userId)">{{ postDetail.username }}</a> 关注
              <a-divider type="vertical" />
              <a-icon type="eye" style="margin-right: 10px;margin-left: 40px" />
              {{ postDetail.pageviews }} 访问
              <a-divider type="vertical" />
              <a-icon type="message" style="margin-right: 10px" />
              <span v-if="postDetail.collect === 0">{{ postDetail.reply }}</span>
              <span v-else>{{ postDetail.reply / postDetail.collect }}</span> 回复
              <a-divider type="vertical" />
              <a-icon v-if="user.userId && user.roleId != 76 && collectPost === 0" type="star" style="margin-right: 10px;cursor: pointer" @click="collectPostCheck(0)"/>
              <a-icon v-if="user.userId && user.roleId != 76 && collectPost > 0" type="star" style="margin-right: 10px;color: red;cursor: pointer" @click="collectPostCheck(1)"/>
              {{ postDetail.collect }} 收藏
              <a-divider type="vertical" />
              {{ timeFormat(postDetail.createDate) }}
            </div>
            <div style="margin: 25px 50px;font-size: 15px;line-height: 1.6;word-break: break-word;letter-spacing: 1px;text-indent: 30px">
              {{ postDetail.content }}
            </div>
            <div style="margin: 25px 50px;height: 100px">
              <a-upload
                name="avatar"
                action="http://127.0.0.1:9527/file/fileUpload/"
                list-type="picture-card"
                :file-list="fileList"
                @preview="handlePreview"
              >
              </a-upload>
              <a-modal :visible="previewVisible" :footer="null" @cancel="handleCancel">
                <img alt="example" style="width: 100%" :src="previewImage" />
              </a-modal>
            </div>
            <div style="margin: 25px 50px;">
              <a-list
                class="comment-list"
                :pagination="pagination"
                :header="`${replyList.length} 回复`"
                item-layout="horizontal"
                :data-source="replyList"
              >
                <a-list-item slot="renderItem" slot-scope="item, index">
                  <a-comment shape="square" @click="pushToDetail(item.userId)">
                    <div slot="avatar" class="comment-avatar-wrapper">
                      <a-avatar :src="'http://127.0.0.1:9527/imagesWeb/' + item.images" />
                    </div>
                    <div slot="author" class="comment-author-wrapper">
                      <span class="comment-username">{{ item.username }}</span>
                      <a-tag v-if="item.userLevel" :color="getUserLevelColor(item.userLevel)" class="comment-level-tag">
                        {{ item.userLevel }}
                      </a-tag>
                    </div>
                    <template slot="actions">
                      <span @click.stop="replyUserAdd(item)">回复</span>
                    </template>
                    <p slot="content" style="white-space: pre-line;">
                      {{ item.content }}
                    </p>
                    <a-tooltip slot="datetime" :title="item.sendCreate">
                      <span>{{ timeFormat(item.sendCreate) }}</span>
                    </a-tooltip>
                  </a-comment>
                </a-list-item>
              </a-list>
              <div style="margin-bottom: 200px;margin-top: 50px" v-if="user.userId && user.roleId != 76">
                <a-textarea
                  v-model="replyContent"
                  placeholder="Controlled autosize"
                  :rows="5"
                />
                <a-button type="primary" style="float: right;margin-top: 15px" @click="commit">
                  提交
                </a-button>
              </div>
            </div>
          </div>
        </a-col>
        <a-col :span="8">
          <div style="padding: 0 22px">
            <a-list item-layout="vertical" :pagination="false" :data-source="bulletinList">
              <a-list-item slot="renderItem" key="item.title" slot-scope="item, index">
                <template slot="actions">
                  <span key="message" style="font-size: 13px">
                    <a-icon type="message" style="margin-right: 8px" />
                    {{ item.title }}
                  </span>
                </template>
                <a-list-item-meta :description="item.content.slice(0, 30) + '...'" style="font-size: 13px">
                </a-list-item-meta>
                <a slot="actions">
                  <span @click="orderViewOpen(item)">详情</span>
                </a>
              </a-list-item>
            </a-list>
          </div>
        </a-col>
      </a-card>
    </a-row>
    <post-add
      v-if="postAdd.visiable"
      @close="handlepostAddClose"
      @success="handlepostAddSuccess"
      :postAddVisiable="postAdd.visiable"
      :tagList="tagListData">
    </post-add>
    <post-edit
      ref="postEdit"
      @close="handlepostEditClose"
      @success="handlepostEditSuccess"
      :postEditVisiable="postEdit.visiable"
      :tagList="tagListData">
    </post-edit>
    <clothes-view
      @close="handleorderViewClose"
      :orderShow="orderView.visiable"
      :orderData="orderView.data">
    </clothes-view>
  </div>
</template>
<script>
import HeadInfo from '@/views/common/HeadInfo'
import PostAdd from './admin/post/PostAdd'
import PostEdit from './admin/post/PostEdit'
import {mapState} from 'vuex'
import moment from 'moment'
import ClothesView from './admin/clothes/ClothesView.vue'
moment.locale('zh-cn')
function getBase64 (file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.readAsDataURL(file)
    reader.onload = () => resolve(reader.result)
    reader.onerror = error => reject(error)
  })
}

export default {
  name: 'HomePage',
  components: {ClothesView, HeadInfo, PostAdd, PostEdit},
  data () {
    return {
      searchVipFlag: false,
      name: null,
      newsPage: 0,
      newsContent: '',
      newsList: [],
      todayIp: '',
      todayVisitCount: '',
      totalVisitCount: '',
      userRole: '',
      userDept: '',
      lastLoginTime: '',
      welcomeMessage: '',
      tagList: [],
      tagListData: [],
      postList: [],
      replyList: [],
      postDetail: null,
      tabName: '',
      tabKey: '',
      postDetailShow: false,
      pagination: {
        pageSize: 20
      },
      loading: false,
      fileList: [],
      previewVisible: false,
      previewImage: '',
      replyContent: '',
      replyUser: null,
      collectPost: 0,
      collectUser: 0,
      postAdd: {
        visiable: false
      },
      postEdit: {
        visiable: false
      },
      homeImage: ['SA1767408413573.jpg'],
      bulletinList: [],
      orderView: {
        visiable: false,
        data: null
      }
    }
  },
  watch: {
    replyContent: function (value) {
      if (value === '') {
        this.replyUser = null
      }
    }
  },
  computed: {
    ...mapState({
      multipage: state => state.setting.multipage,
      user: state => state.account.user
    }),
    avatar () {
      if (!this.user || !this.user.avatar) {
        return ''
      }
      return `static/avatar/${this.user.avatar}`
    }
  },
  methods: {
    pushToDetail (userId) {
      this.$router.push({
        path: '/userDetail',
        query: {
          id: userId
        }
      })
    },
    goManage () {
      this.$router.push('/center')
    },
    selectHomeImages () {
      this.$get(`/cos/home-info/data`).then((r) => {
        this.homeImage = r.data.home.images.split(',')
        this.bulletinList = r.data.clothes
        console.log(this.homeImage)
        console.log(this.bulletinList)
      })
    },
    orderViewOpen (row) {
      this.orderView.data = row
      this.orderView.visiable = true
    },
    handleorderViewClose () {
      this.orderView.visiable = false
    },
    recommendList () {
      if (!this.user || !this.user.userId) {
        return
      }
      this.loading = true
      this.$get(`/cos/post-info/recommend/${this.user.userId}`).then((r) => {
        this.postList = r.data.data
        setTimeout(() => {
          this.loading = false
        }, 500)
      })
    },
    collectUserCheck (deleteFlag) {
      this.$post(`/cos/focus-info`, {userId: this.user.userId, collectUserId: this.postDetail.userId, deleteFlag}).then((r) => {
        this.postReplyDetail(this.postDetail)
        this.$message.success(deleteFlag === 0 ? '关注成功！' : '取消关注成功！')
      })
    },
    collectPostCheck (deleteFlag) {
      this.$post(`/cos/collect-info`, {userId: this.user.userId, postId: this.postDetail.id, deleteFlag}).then((r) => {
        this.postReplyDetail(this.postDetail)
        this.$message.success(deleteFlag === 0 ? '收藏贴子成功！' : '取消收藏成功！')
      })
    },
    collectByUser (postId) {
      if (!this.user || !this.user.userId) {
        this.collectPost = 0
        this.collectUser = 0
        return
      }
      this.$get(`/cos/post-info/collcet`, {userId: this.user.userId, postId}).then((r) => {
        this.collectPost = r.data.collect
        this.collectUser = r.data.focus
      })
    },
    commit () {
      if (this.replyContent !== '') {
        let data = {userId: this.user.userId, content: this.replyContent, postId: this.postDetail.id, replyUserId: this.replyUser}
        this.$post(`/cos/reply-info`, data).then((r) => {
          if (r.data.code === 500) {
            this.$message.error(r.data.msg)
          } else {
            this.postReplyDetail(this.postDetail)
            this.replyContent = ''
          }
        })
      } else {
        this.$message.error('请填写评论！')
      }
    },
    replyUserAdd (reply) {
      this.replyUser = reply.userId
      this.replyContent = this.replyContent + '@' + reply.username
    },
    postReplyDetail (post) {
      this.postInfoDetail(post.id)
      this.collectByUser(post.id)
      this.replyUser = []
      this.fileList = []
      this.$get(`/cos/reply-info/list/${post.id}`).then((r) => {
        this.replyList = r.data.data
        this.postDetailShow = true
      })
    },
    postInfoDetail (postId) {
      this.$get(`/cos/post-info/${postId}`).then((r) => {
        this.postDetail = r.data
        this.imagesInit(this.postDetail.images)
      })
    },
    tabChange (key) {
      this.tabName = this.tagList.find(o => o.id === key).name
      this.tabKey = key
      if (key !== 9999 && key !== -1) {
        this.getPostList(key)
        if (this.tagList[this.tagList.length - 1].id === 9999) {
          this.tagList.pop()
        }
      }
      if (key === -1) {
        if (this.user && this.user.userId) {
          this.recommendList()
        } else if (this.tagList.length > 1) {
          this.getPostList(this.tagList[1].id)
        } else {
          this.postList = []
          this.loading = false
        }
      }
    },
    imagesInit (images) {
      if (images !== null && images !== '') {
        let imageList = []
        images.split(',').forEach((image, index) => {
          imageList.push({uid: index, name: image, status: 'done', url: 'http://127.0.0.1:9527/imagesWeb/' + image})
        })
        this.fileList = imageList
      }
    },
    async handlePreview (file) {
      if (!file.url && !file.preview) {
        file.preview = await getBase64(file.originFileObj)
      }
      this.previewImage = file.url || file.preview
      this.previewVisible = true
    },
    handleCancel () {
      this.previewVisible = false
    },
    getPostList (tagId) {
      this.loading = true
      this.$get(`/cos/post-info/tag/${tagId}`).then((r) => {
        this.postList = r.data.data
        setTimeout(() => {
          this.loading = false
        }, 500)
      })
    },
    getTagList () {
      this.$get('/cos/tag-info/list').then((r) => {
        this.tagList = [{id: -1, name: '推荐'}]
        this.tagList.push.apply(this.tagList, r.data.data)
        console.log(this.tagList)
        if (this.tagList.length !== 0) {
          if (this.user && this.user.userId) {
            this.tabChange(this.tagList[0].id)
          } else if (this.tagList.length > 1) {
            this.tabChange(this.tagList[1].id)
          } else {
            this.postList = []
            this.loading = false
          }
        }
        let tagListData = []
        r.data.data.forEach(item => {
          tagListData.push({label: item.name, value: item.id})
        })
        this.tagListData = tagListData
      })
    },
    welcome () {
      const date = new Date()
      const hour = date.getHours()
      let time = hour < 6 ? '早上好' : (hour <= 11 ? '上午好' : (hour <= 13 ? '中午好' : (hour <= 18 ? '下午好' : '晚上好')))
      const name = (this.user && this.user.username) ? this.user.username : '游客'
      return `${time}，${name}`
    },
    timeFormat (time) {
      var nowTime = new Date()
      var day = nowTime.getDate()
      var hours = parseInt(nowTime.getHours())
      var minutes = nowTime.getMinutes()
      // 开始分解付入的时间
      var timeday = time.substring(8, 10)
      var timehours = parseInt(time.substring(11, 13))
      var timeminutes = time.substring(14, 16)
      // eslint-disable-next-line camelcase
      var d_day = Math.abs(day - timeday)
      // eslint-disable-next-line camelcase
      var d_hours = hours - timehours
      // eslint-disable-next-line camelcase
      var d_minutes = Math.abs(minutes - timeminutes)
      // eslint-disable-next-line camelcase
      if (d_day <= 1) {
        // eslint-disable-next-line camelcase
        switch (d_day) {
          case 0:
            // eslint-disable-next-line camelcase
            if (d_hours === 0 && d_minutes > 0) {
              // eslint-disable-next-line camelcase
              return d_minutes + '分钟前'
              // eslint-disable-next-line camelcase
            } else if (d_hours === 0 && d_minutes === 0) {
              return '1分钟前'
            } else {
              // eslint-disable-next-line camelcase
              return Math.abs(d_hours) + '小时前'
            }
            // eslint-disable-next-line no-unreachable
            break
          case 1:
            // eslint-disable-next-line camelcase
            if (d_hours < 0) {
              // eslint-disable-next-line camelcase
              return (24 + d_hours) + '小时前'
            } else {
              // eslint-disable-next-line camelcase
              return d_day + '天前'
            }
            // eslint-disable-next-line no-unreachable
            break
        }
        // eslint-disable-next-line camelcase
      } else if (d_day > 1 && d_day < 10) {
        // eslint-disable-next-line camelcase
        return d_day + '天前'
      } else {
        return time
      }
    },
    add () {
      this.postAdd.visiable = true
    },
    handlepostAddClose () {
      this.postAdd.visiable = false
    },
    handlepostAddSuccess () {
      this.postAdd.visiable = false
      this.$message.success('新增贴子成功')
      this.getPostList(this.tabKey)
    },
    edit (record) {
      this.$refs.postEdit.setFormValues(record)
      this.postEdit.visiable = true
    },
    handlepostEditClose () {
      this.postEdit.visiable = false
    },
    handlepostEditSuccess () {
      this.postEdit.visiable = false
      this.$message.success('修改贴子成功')
      this.postReplyDetail(this.postDetail)
    },
    getNewList () {
      this.$get(`/cos/bulletin-info/list`).then((r) => {
        this.newsList = r.data.data
        if (this.newsList.length !== 0) {
          this.newsContent = `《${this.newsList[0].title}》 ${this.newsList[0].content}`
        }
      })
    },
    newsNext () {
      if (this.newsPage + 1 === this.newsList.length) {
        this.newsPage = 0
      } else {
        this.newsPage += 1
      }
      this.newsContent = `《${this.newsList[this.newsPage].title}》 ${this.newsList[this.newsPage].content}`
    },
    onSearch (key) {
      if (key !== '' || this.name !== '' || this.searchVipFlag) {
        this.loading = true
        if (this.tagList[this.tagList.length - 1].id !== 9999) {
          this.tagList.push({id: 9999, name: '搜索'})
        }
        this.tabKey = 9999
        this.tabName = '搜索'

        const searchParams = {
          key: key || null,
          name: this.name || null,
          vipFlag: this.searchVipFlag ? '大V用户' : null
        }

        this.$get('/cos/post-info/querySearch', searchParams).then((r) => {
          this.postList = r.data.data || []
          setTimeout(() => {
            this.loading = false
          }, 500)
        }).catch(() => {
          this.loading = false
        })
      } else {
        this.$message.warning('请输入搜索条件')
      }
    },
    getUserLevelColor (level) {
      const levelMap = {
        '小白用户': 'blue',
        '高级用户': 'green',
        '大V用户': 'orange'
      }
      return levelMap[level] || 'default'
    }
  },
  mounted () {
    this.selectHomeImages()
    this.getTagList()
    this.getNewList()
    this.welcomeMessage = this.welcome()
    if (this.user && this.user.username) {
      this.$get(`index/${this.user.username}`).then((r) => {
        let data = r.data.data
        this.todayIp = data.todayIp
        this.todayVisitCount = data.todayVisitCount
        this.totalVisitCount = data.totalVisitCount
        let sevenVisitCount = []
        let dateArr = []
        for (let i = 6; i >= 0; i--) {
          let time = moment().subtract(i, 'days').format('MM-DD')
          let contain = false
          for (let o of data.lastSevenVisitCount) {
            if (o.days === time) {
              contain = true
              sevenVisitCount.push(o.count)
            }
          }
          if (!contain) {
            sevenVisitCount.push(0)
          }
          dateArr.push(time)
        }
        let sevenUserVistCount = []
        for (let i = 6; i >= 0; i--) {
          let time = moment().subtract(i, 'days').format('MM-DD')
          let contain = false
          for (let o of data.lastSevenUserVisitCount) {
            if (o.days === time) {
              contain = true
              sevenUserVistCount.push(o.count)
            }
          }
          if (!contain) {
            sevenUserVistCount.push(0)
          }
        }
      }).catch((r) => {
        console.error(r)
        this.$message.error('获取首页信息失败')
      })
    }
  }
}
</script>
<style lang="less">
.home-page {
  background: linear-gradient(135deg, #e9f8e7 0%, #d4f1d1 100%);
  min-height: 100vh;
  padding: 20px 0;

  // 卡片统一美化
  .ant-card {
    border-radius: 12px;
    box-shadow: 0 4px 12px rgba(0,0,0,0.05);
    border: none;
  }

  // 轮播图样式优化
  .ant-carousel {
    .slick-slide {
      border-radius: 12px;
      overflow: hidden;
    }
    img {
      transition: transform 0.3s ease;
      &:hover {
        transform: scale(1.02);
      }
    }
  }

  // 景区推荐卡片
  .scenic-recommend-card {
    height: 350px;
    overflow-y: auto;

    .ant-card-head {
      background: linear-gradient(90deg, #4CAF50, #8BC34A);
      color: white;
      border-radius: 12px 12px 0 0;
      .ant-card-head-title {
        color: white;
        font-weight: bold;
      }
    }
  }

  // 头部信息区域
  .head-info {
    margin-bottom: 1.5rem;
    .head-info-card {
      background: linear-gradient(135deg, #ffffff 0%, #f8fdf7 100%);
      border-radius: 12px;
      padding: 1.5rem;
      border: 1px solid #e8f5e9;

      .head-info-welcome {
        font-size: 1.2rem;
        font-weight: 600;
        color: #2e7d32;
        margin-bottom: 0.5rem;
      }

      .head-info-desc {
        color: #66bb6a;
        font-size: 0.9rem;
        padding: 0.3rem 0;
      }

      .head-info-time {
        color: #81c784;
        font-size: 0.85rem;
        padding: 0.3rem 0;
      }
    }
  }

  // 统计信息区域
  .count-info {
    .head-info-card {
      background: rgba(255, 255, 255, 0.9);
      backdrop-filter: blur(10px);
      border-radius: 12px;
      padding: 1.5rem;
      border: 1px solid rgba(232, 245, 233, 0.5);
    }

    // 搜索框美化
    .ant-input-search {
      .ant-input {
        border-radius: 20px;
        border: 1px solid #c8e6c9;
        &:focus {
          border-color: #4CAF50;
          box-shadow: 0 0 0 2px rgba(76, 175, 80, 0.2);
        }
      }
      .ant-input-search-button {
        border-radius: 20px;
        background: #4CAF50;
        border-color: #4CAF50;
      }
    }

    // 标签页美化
    .ant-tabs {
      .ant-tabs-tab {
        padding: 12px 16px !important;
        border-radius: 6px;
        margin-right: 8px;
        transition: all 0.3s;
        &:hover {
          background: #e8f5e9;
        }
      }
      .ant-tabs-tab-active {
        background: #e8f5e9;
        color: #4CAF50;
        font-weight: 500;
        border: 1px solid #c8e6c9;
      }
      .ant-tabs-ink-bar {
        background: #4CAF50;
      }
    }

    // 列表项美化
    .ant-list-item {
      padding: 1.2rem 1.5rem !important;
      border-bottom: 1px solid #f0f0f0;
      transition: all 0.3s;
      border-radius: 8px;
      margin-bottom: 12px;

      &:hover {
        background: #f1f8e9;
        transform: translateY(-2px);
        box-shadow: 0 4px 8px rgba(0,0,0,0.08);
      }

      .ant-list-item-action {
        li {
          span {
            display: flex;
            align-items: center;
            color: #66bb6a;
            font-size: 0.9rem;

            .anticon {
              margin-right: 5px;
            }
          }
        }
      }

      .ant-list-item-meta-title {
        a {
          color: #2e7d32;
          font-weight: 500;
          font-size: 1.1rem;
          transition: color 0.3s;

          &:hover {
            color: #4CAF50;
          }
        }
      }

      .ant-list-item-meta-description {
        color: #757575;
        line-height: 1.6;
      }
    }
  }

  // 帖子详情页面
  .post-detail-section {
    background: white;
    border-radius: 12px;
    padding: 2rem;
    box-shadow: 0 4px 12px rgba(0,0,0,0.05);

    .post-title {
      font-size: 1.8rem;
      color: #1b5e20;
      font-weight: 600;
      margin-bottom: 1.5rem;
      line-height: 1.4;
    }

    .post-meta {
      background: #f5fbf5;
      padding: 1rem;
      border-radius: 8px;
      margin-bottom: 1.5rem;
      border-left: 4px solid #4CAF50;

      .meta-item {
        display: inline-flex;
        align-items: center;
        margin-right: 1.5rem;
        color: #66bb6a;

        .anticon {
          margin-right: 8px;
        }
      }
    }

    .post-content {
      font-size: 1.05rem;
      line-height: 1.8;
      color: #424242;
      margin-bottom: 2rem;
      padding: 1.5rem;
      background: #fafafa;
      border-radius: 8px;
      border-left: 4px solid #81c784;
    }

    // 评论区域
    .comment-list {
      .ant-list-header {
        background: #e8f5e9;
        border-radius: 8px 8px 0 0;
        font-weight: 500;
        color: #2e7d32;
      }

      .ant-comment {
        padding: 1rem 0;
        border-bottom: 1px solid #eee;

        .ant-comment-content {
          background: #f1f8e9;
          padding: 1rem;
          border-radius: 8px;

          .ant-comment-author-time {
            color: #9e9e9e;
            font-size: 0.85rem;
          }
        }
      }
    }

    // 回复框
    .reply-section {
      margin-top: 2rem;
      padding: 1.5rem;
      background: #f1f8e9;
      border-radius: 8px;

      .ant-btn-primary {
        background: #4CAF50;
        border-color: #4CAF50;
        border-radius: 6px;
        &:hover {
          background: #43a047;
          border-color: #43a047;
        }
      }
    }
  }

  // 按钮统一美化
  .ant-btn {
    border-radius: 6px;
    font-weight: 500;

    &.ant-btn-primary {
      background: #4CAF50;
      border-color: #4CAF50;
      &:hover {
        background: #43a047;
        border-color: #43a047;
      }
    }
  }

  // 提示框美化
  .ant-alert {
    border-radius: 8px;
    border: none;
    background: rgba(76, 175, 80, 0.1);
    .ant-alert-message {
      font-weight: 500;
      color: #2e7d32;
    }
  }
}

// 搜索框美化
.search-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: linear-gradient(135deg, #f8fdf7 0%, #e8f5e9 100%);
  border-radius: 8px;
  border: 1px solid #c8e6c9;
  margin-top: 10px;
  box-shadow: 0 2px 8px rgba(76, 175, 80, 0.08);
  transition: all 0.3s ease;

  &:hover {
    box-shadow: 0 4px 12px rgba(76, 175, 80, 0.15);
    border-color: #81c784;
  }

  .search-checkbox {
    color: #2e7d32;
    font-weight: 500;
    white-space: nowrap;

    ::v-deep .ant-checkbox-checked .ant-checkbox-inner {
      background-color: #4CAF50;
      border-color: #4CAF50;
    }

    ::v-deep .ant-checkbox:hover .ant-checkbox-inner {
      border-color: #4CAF50;
    }
  }

  .search-input {
    flex: 0 0 180px;

    ::v-deep .ant-input {
      border-radius: 20px;
      border: 1px solid #c8e6c9;
      transition: all 0.3s;

      &:focus {
        border-color: #4CAF50;
        box-shadow: 0 0 0 2px rgba(76, 175, 80, 0.2);
      }

      &::placeholder {
        color: #a5d6a7;
      }
    }
  }

  .search-input-search {
    flex: 0 0 220px;

    ::v-deep .ant-input {
      border-radius: 20px 0 0 20px;
      border-right: none;
      border-color: #c8e6c9;

      &:focus {
        border-color: #4CAF50;
        box-shadow: none;
      }
    }

    ::v-deep .ant-input-search-button {
      border-radius: 0 20px 20px 0;
      background: linear-gradient(135deg, #4CAF50, #66bb6a);
      border-color: #4CAF50;
      color: white;
      transition: all 0.3s;

      &:hover {
        background: linear-gradient(135deg, #43a047, #4CAF50);
        border-color: #43a047;
      }
    }
  }
}

// 列表项美化
.ant-list-item {
  padding: 1.2rem 1.5rem !important;
  border-bottom: 1px solid #f0f0f0;
  transition: all 0.3s;
  border-radius: 8px;
  margin-bottom: 12px;

  &:hover {
    background: #f1f8e9;
    transform: translateY(-2px);
    box-shadow: 0 4px 8px rgba(0,0,0,0.08);
  }

  .ant-list-item-action {
    li {
      span {
        display: flex;
        align-items: center;
        color: #66bb6a;
        font-size: 0.9rem;

        .anticon {
          margin-right: 5px;
        }
      }
    }
  }

  .post-title-wrapper {
    display: flex;
    align-items: center;
    gap: 10px;
    flex-wrap: wrap;

    .post-title {
      color: #2e7d32;
      font-weight: 500;
      font-size: 1.1rem;
      transition: color 0.3s;
      cursor: pointer;

      &:hover {
        color: #4CAF50;
      }
    }

    .user-level-tag {
      font-size: 0.75rem;
      padding: 2px 8px;
      border-radius: 10px;
      font-weight: 500;
      margin-left: 4px;
    }
  }

  .avatar-wrapper {
    display: flex;
    align-items: center;
  }

  .ant-list-item-meta-description {
    color: #757575;
    line-height: 1.6;
  }
}

.comment-list {
  .ant-list-header {
    background: #e8f5e9;
    border-radius: 8px 8px 0 0;
    font-weight: 500;
    color: #2e7d32;
  }

  .ant-comment {
    padding: 1rem 0;
    border-bottom: 1px solid #eee;

    .comment-avatar-wrapper {
      cursor: pointer;
      transition: transform 0.3s;

      &:hover {
        transform: scale(1.1);
      }
    }

    .comment-author-wrapper {
      display: flex;
      align-items: center;
      gap: 8px;
      flex-wrap: wrap;

      .comment-username {
        color: #2e7d32;
        font-weight: 500;
        cursor: pointer;
        transition: color 0.3s;

        &:hover {
          color: #4CAF50;
        }
      }

      .comment-level-tag {
        font-size: 0.7rem;
        padding: 1px 6px;
        border-radius: 8px;
        font-weight: 500;
      }
    }

    .ant-comment-content {
      background: #f1f8e9;
      padding: 1rem;
      border-radius: 8px;

      .ant-comment-author-time {
        color: #9e9e9e;
        font-size: 0.85rem;
      }
    }
  }
}
</style>
